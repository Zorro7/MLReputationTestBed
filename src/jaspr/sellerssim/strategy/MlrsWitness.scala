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

      new TrustAssessment(request, if (predictions.isEmpty) 0d else predictions.sum / predictions.size)
    }
  }


  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRatings = context.client.getProvenance[BuyerRecord](context.client)
    val witnessRatings = network.gatherProvenance[BuyerRecord](context.client)

    val freakEventLikelihood = directRatings.groupBy(_.event.name).mapValues(_.size / directRatings.size.toDouble)

    if (witnessRatings.isEmpty) null
    else if (directRatings.isEmpty) {
      val model = makeMlrsModel(witnessRatings, baseWitness, makeWitnessRow(_: BuyerRecord))
      new MlrsWitnessInit(context, model.model, model.train, model.attVals, witnessRatings, freakEventLikelihood)
    } else {
      val imputationModel = makeMlrsModel(directRatings, baseImputation, makeImputationRow)
      val model = makeMlrsModel(
        witnessRatings,
        baseWitness,
        makeWitnessRow(_: BuyerRecord, imputationModel)
      )
      new MlrsWitnessInit(context, model.model, model.train, model.attVals, witnessRatings, freakEventLikelihood)
    }
  }

  def makeWitnessRow(r: BuyerRecord,
                      imputationModel: MlrsModel): List[Any] = {
    val imputationRow = makeImputationTestRow(r)
    val imputationQuery = convertRowToInstance(imputationRow, imputationModel.attVals, imputationModel.train)
    val imputationResult = imputationModel.model.classifyInstance(imputationQuery)
    imputationResult ::
      r.client.id.toString ::
      r.service.payload.name :: // service identifier (client context)
      r.rating ::
      r.provider.advertProperties.values.map(_.value).toList
  }

  def makeWitnessRow(r: BuyerRecord): Seq[Any] = {
    (if (discreteClass) discretizeInt(r.rating) else r.rating) ::
      r.client.id.toString ::
      r.service.payload.name :: // service identifier (client context)
      r.rating ::
      r.provider.advertProperties.values.map(_.value).toList
  }

  def makeWitnessTestRows(init: MlrsInit, request: ServiceRequest, witnessRatings: Seq[BuyerRecord], fe: String): Iterable[List[Any]] = {
    val ret = for (r <- witnessRatings.filter(x => x.provider == request.provider && x.service.payload.name == request.payload.name)) yield {
      0 ::
        r.client.id.toString :: // witness
        request.payload.name ::  // service context
        r.rating ::
        request.provider.advertProperties.values.map(_.value).toList
    }
    ret
  }

  def makeImputationRow(x: BuyerRecord): Seq[Any] = {
    (if (discreteClass) discretizeInt(x.rating) else x.rating) :: // target rating
      x.service.payload.name :: // service identifier (client context)
      x.provider.advertProperties.values.map(_.value).toList // provider features
  }

  def makeImputationTestRow(witnessRating: BuyerRecord): List[Any] = {
//    for (fe <- init.asInstanceOf[MlrsStrategyInit].freakEventLikelihood.keys) yield {
      0 ::
//        witnessRating.provider.id.toString ::
        witnessRating.service.payload.name ::
        witnessRating.provider.advertProperties.values.map(_.value).toList
//    }
  }



}
