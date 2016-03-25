package jaspr.sellerssim.strategy

import jaspr.core.Network
import jaspr.core.service.{ClientContext, TrustAssessment, ServiceRequest}
import jaspr.core.strategy.{StrategyInit, Exploration}
import jaspr.sellerssim.service.BuyerRecord
import jaspr.strategy.CompositionStrategy
import weka.classifiers.functions.LinearRegression
import weka.classifiers.{AbstractClassifier, Classifier}
import weka.core.Instances

import scala.collection.mutable

/**
 * Created by phil on 04/11/15.
 */
trait MlrsWitness extends CompositionStrategy with Exploration with MlrsCore {

  override val discreteClass: Boolean
  def baseImputation: Classifier
  def baseWitness: Classifier


  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[MlrsInit]

    if (init.witnessInit == null) {
      new TrustAssessment(request, 0d)
    } else {
      val witnessModel = init.witnessInit.witnessModel
      val witnessTrain = init.witnessInit.witnessTrain
      val witnessAttVals = init.witnessInit.witnessAttVals
      val witnessRatings = init.witnessInit.witnessRatings

      val rows: Iterable[List[Any]] = makeWitnessTestRows(init, request, witnessRatings, "null")
      val queries = convertRowsToInstances(rows, witnessAttVals, witnessTrain)
      val preds = queries.map(q => witnessModel.classifyInstance(q))
      val predictions =
        if (discreteClass) preds.map(x => witnessTrain.classAttribute().value(x.toInt).toDouble)
        else preds

      new TrustAssessment(request, if (predictions.isEmpty) 0d else predictions.sum)
    }
  }


  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRatings = context.client.getProvenance[BuyerRecord](context.client)
    val witnessRatings = network.gatherProvenance[BuyerRecord](context.client)

    val freakEventLikelihood = directRatings.groupBy(_.event.name).mapValues(_.size / directRatings.size.toDouble)

    if (witnessRatings.isEmpty) null
    else if (directRatings.isEmpty) {
      val witnessRows = makeWitnessRows(witnessRatings)
      val witnessAttVals: Iterable[mutable.Map[Any, Double]] = List.fill(witnessRows.head.size)(mutable.Map[Any, Double]())
      val doubleWitnessRows = convertRowsToDouble(witnessRows, witnessAttVals)
      val witnessAtts = makeAtts(witnessRows.head, witnessAttVals)
      val witnessTrain = makeInstances(witnessAtts, doubleWitnessRows)
      val witnessModel = AbstractClassifier.makeCopy(baseWitness)
      witnessModel.buildClassifier(witnessTrain)

      new MlrsWitnessInit(context, witnessModel, witnessTrain, witnessAttVals, witnessRatings, freakEventLikelihood)
    } else {
      val imputationRows = makeImputationRows(directRatings)
      val imputationAttVals: Iterable[mutable.Map[Any, Double]] = List.fill(imputationRows.head.size)(mutable.Map[Any, Double]())
      val doubleImputationRows = convertRowsToDouble(imputationRows, imputationAttVals)
      val imputationAtts = makeAtts(imputationRows.head, imputationAttVals)
      val imputationTrain = makeInstances(imputationAtts, doubleImputationRows)
      val imputationModel = AbstractClassifier.makeCopy(baseImputation)
      imputationModel.buildClassifier(imputationTrain)

      val witnessRows =
        if (imputationModel.isInstanceOf[LinearRegression] && (
            imputationModel.asInstanceOf[LinearRegression].coefficients().count(_ != 0) == 1 ||
            imputationModel.asInstanceOf[LinearRegression].coefficients().exists(_.isNaN))) {
          makeWitnessRows(witnessRatings)
        } else {
          makeWitnessRows(witnessRatings, imputationModel, imputationAttVals, imputationTrain)
        }
      val witnessAttVals: Iterable[mutable.Map[Any, Double]] = List.fill(witnessRows.head.size)(mutable.Map[Any, Double]())
      val doubleWitnessRows = convertRowsToDouble(witnessRows, witnessAttVals)
      val witnessAtts = makeAtts(witnessRows.head, witnessAttVals)
      val witnessTrain = makeInstances(witnessAtts, doubleWitnessRows)
      val witnessModel = AbstractClassifier.makeCopy(baseWitness)
      witnessModel.buildClassifier(witnessTrain)

      new MlrsWitnessInit(context, witnessModel, witnessTrain, witnessAttVals, witnessRatings, freakEventLikelihood)
    }
  }

  def makeWitnessRows(witnessRatings: Seq[BuyerRecord],
                      imputationModel: Classifier,
                      imputationAttVals: Iterable[mutable.Map[Any,Double]],
                      imputationTrain: Instances): Iterable[List[Any]] = {
    for (r <- witnessRatings) yield {
      val imputationRow = makeImputationTestRow(r)
      val imputationQuery = convertRowToInstance(imputationRow, imputationAttVals, imputationTrain)
      imputationModel.classifyInstance(imputationQuery) ::
        r.client.id.toString ::
//        r.provider.id.toString ::
        r.service.payload.name :: // service identifier (client context)
//        r.provider.asInstanceOf[OrganisationAgent].organisation.id.toString ::
//        r.asInstanceOf[MlrsRating].freakEvent :: // mitigation (provider context)
        r.rating ::
        r.provider.advertProperties.values.map(_.value).toList
    }
  }

  def makeWitnessRows(witnessRatings: Seq[BuyerRecord]): Iterable[List[Any]] = {
    for (r <- witnessRatings) yield {
      r.rating ::
        r.client.id.toString ::
//        r.provider.id.toString ::
        r.service.payload.name :: // service identifier (client context)
        //        r.provider.asInstanceOf[OrganisationAgent].organisation.id.toString ::
        //        r.asInstanceOf[MlrsRating].freakEvent :: // mitigation (provider context)
        r.rating ::
        r.provider.advertProperties.values.map(_.value).toList
    }
  }

  def makeWitnessTestRows(init: MlrsInit, request: ServiceRequest, witnessRatings: Seq[BuyerRecord], fe: String): Iterable[List[Any]] = {
    val ret = for (r <- witnessRatings.filter(x => x.provider == request.provider && x.service.payload.name == init.context.payload.name)) yield {
      0 ::
        r.client.id.toString :: // witness
//        provider.id.toString :: // provider
        request.payload.name ::  // service context
//        provider.asInstanceOf[OrganisationAgent].organisation.id.toString ::
//        fe :: // mitigation
        r.rating ::
          request.provider.advertProperties.values.map(_.value).toList
    }
    ret
  }

  def makeImputationRows(directRatings: Seq[BuyerRecord]): Iterable[List[Any]] = {
    directRatings.map(x => {
      (if (discreteClass) discretizeInt(x.rating) else x.rating) :: // target rating
//        x.provider.id.toString :: // provider identifier
        x.service.payload.name :: // service identifier (client context)
//        x.provider.asInstanceOf[OrganisationAgent].organisation.id.toString ::
        x.provider.advertProperties.values.map(_.value).toList // provider features
    })
  }

  def makeImputationTestRow(witnessRating: BuyerRecord): List[Any] = {
//    for (fe <- init.asInstanceOf[MlrsStrategyInit].freakEventLikelihood.keys) yield {
      0 ::
//        witnessRating.provider.id.toString ::
        witnessRating.service.payload.name ::
//        witnessRating.provider.asInstanceOf[OrganisationAgent].organisation.id.toString ::
        witnessRating.provider.advertProperties.values.map(_.value).toList
//    }
  }



}
