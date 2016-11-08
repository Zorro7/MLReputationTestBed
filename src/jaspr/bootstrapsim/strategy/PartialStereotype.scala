package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.{BootRecord, Observations, Trustee}
import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.CompositionStrategy
import jaspr.strategy.mlr.MlrModel
import jaspr.utilities.BetaDistribution
import meka.classifiers.multilabel.BR
import weka.classifiers.{AbstractClassifier, Classifier, bayes}
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.trees.RandomForest

import scala.collection.mutable

/**
  * Created by phil on 27/10/16.
  */
class PartialStereotype(baseLearner: Classifier,
                        override val numBins: Int,
                        val _witnessWeight: Double = 2d,
                        val witnessStereotypes: Boolean = true,
                        val subjectiveStereotypes: Boolean = false,
                        val hideTrusteeIDs: Boolean = false,
                        override val limitedObservations: Boolean = false
                       ) extends CompositionStrategy with Exploration with BRSCore with StereotypeCore {


  override val explorationProbability: Double = 0d
  val discountOpinions: Boolean = false
  val weightStereotypes: Boolean = false
  val ratingStereotype: Boolean = false

  override val goodOpinionThreshold: Double = 0
  override val prior: Double = 0.5
  override val badOpinionThreshold: Double = 0

  // If the trustee ids can't be broadcast, witnesses can't offer their witness-reputation assessments.
  override val witnessWeight: Double = if (hideTrusteeIDs) 0d else _witnessWeight

  override val name: String =
    this.getClass.getSimpleName+"-"+baseLearner.getClass.getSimpleName +"-"+witnessWeight+"-"+explorationProbability+
      (if (discountOpinions) "-discountOpinions" else "")+
      (if (witnessStereotypes) "-witnessStereotypes" else "")+
      (if (weightStereotypes) "-weightStereotypes" else "")+
      (if (ratingStereotype) "-ratingStereotype" else "")+
      (if (subjectiveStereotypes) "-subjectiveStereotypes" else "")+
      (if (hideTrusteeIDs) "-hideTrusteeIDs" else "")+
      (if (limitedObservations) "-limitedObservations" else "")

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[JasprStereotypeInit with Observations]

    val direct = init.directBetas.getOrElse(request.provider, new BetaDistribution(1,1)) // 1,1 for uniform
    val opinions = init.witnessBetas.values.map(
      _.getOrElse(request.provider, new BetaDistribution(0,0)) // 0,0 if the witness had no information about provider
    )

    val beta = getCombinedOpinions(direct, opinions, witnessWeight)

    val directPrior: Double = init.directStereotypeModel match {
      case Some(model) =>
        val row = stereotypeTestRow(init, request)
        val query = convertRowToInstance(row, model.attVals, model.train)
        makePrediction(query, model)
      case None =>
        this.prior
    }

    val witnessStereotypes = init.witnessStereotypeModels.map(x => {
//      val translatedRow = if (subjectiveStereotypes) {
//        val row = stereotypeTestRow(init, request)
//        init.translationModels.get(x._1) match {
//          case Some(model) => 0d :: translate(makeRequestTranslation(request), model).toList
//          case None => row
//        }
//      } else {
//        0 :: objectiveStereotypeRow(x._1, request.provider)
//      }
      val row =
        if (subjectiveStereotypes && // doing subjectivity?
          (hideTrusteeIDs || //can't broadcast trustee ids?
            !init.witnessStereotypeObs.getOrElse(x._1, Nil).contains(request.provider) //witness never seen trustee?
            )) {
//          stereotypeTestRow(init, request) //use truster-observed stereotype
          init.translationModels.get(x._1) match {
            case Some(model) => 0d :: translate(makeRequestTranslation(request), model).toList  //translate trustor observations
            case None => stereotypeTestRow(init, request) //no translation available? use those observed
          }
        } else {
          0 :: objectiveStereotypeRow(x._1, request.provider) //use witness-observed stereotype
        }
      val query = convertRowToInstance(row, x._2.attVals, x._2.train)
      val transRes = makePrediction(query, x._2)
      transRes * init.witnessStereotypeWeights.getOrElse(x._1, 1d)
    })
    val witnessPrior = witnessStereotypes.sum

    val witnessPriorWeight = if (weightStereotypes) init.witnessStereotypeWeights.values.sum else init.witnessStereotypeModels.size.toDouble

    val prior = (directPrior + witnessPrior) / (init.directStereotypeWeight + witnessPriorWeight)

    val score = beta.belief + prior * beta.uncertainty

    new TrustAssessment(init.context, request, score)
  }

  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val directRecords: Seq[BootRecord] = getDirectRecords(network, context)
    val witnessRecords: Seq[BootRecord] = getWitnessRecords(network, context)

    val directBetas: Map[Provider,BetaDistribution] = makeDirectBetas(directRecords)
    val witnessBetas: Map[Client, Map[Provider, BetaDistribution]] = makeWitnessBetas(witnessRecords)

    val weightedWitnessBetas: Map[Client, Map[Provider, BetaDistribution]] =
      if (discountOpinions) weightWitnessBetas(witnessBetas, directBetas)
      else witnessBetas

    val directStereotypeModel: Option[MlrModel] =
      if (directRecords.isEmpty) None
      else {
        val labels =
          if (ratingStereotype) Map[Provider,Double]()
          else directBetas.mapValues(x => x.belief + prior * x.uncertainty)
        Some(makeStereotypeModel(directRecords,labels,baseLearner,stereotypeTrainRow))
      }

    val groupedWitnessRecords: Map[Client,Seq[BootRecord]] = witnessRecords.groupBy(_.service.request.client)

    val witnessStereotypeObs: Map[Client,Seq[Provider]] =
      groupedWitnessRecords.mapValues(_.flatMap(_.observations.keys).distinct)

    val witnessStereotypeModels: Map[Client,MlrModel] =
      if (witnessStereotypes) {
        groupedWitnessRecords.map(wr => wr._1 -> {
          val labels =
            if (ratingStereotype) Map[Provider,Double]()
            else witnessBetas.getOrElse(wr._1, Map()).mapValues(x => x.belief + prior * x.uncertainty)
          makeStereotypeModel(wr._2,labels,baseLearner,stereotypeTrainRow)
        })
      }
      else Map()

    val translationModels: Map[Client,MlrModel] =
      if (witnessStereotypes) makeTranslationModels(directRecords, requests, witnessRecords, baseLearner)
      else Map()

    val directStereotypeWeight: Double =
      if (weightStereotypes) directStereotypeModel match {
        case Some(x) => computeStereotypeWeight(x, directBetas)
        case None => 0d
      } else {
        1d
      }

    val witnessStereotypeWeights: Map[Client,Double] =
      if (weightStereotypes) {
        witnessStereotypeModels.map(sm => sm._1 ->
          computeStereotypeWeight(sm._2,  witnessBetas(sm._1))
        )
      } else {
        Map()
      }

    new JasprStereotypeInit(
      context,
      directBetas,
      weightedWitnessBetas,
      directStereotypeModel,
      witnessStereotypeModels,
      directStereotypeWeight,
      witnessStereotypeWeights,
      witnessStereotypeObs,
      translationModels
    ) with Observations {
      override val possibleRequests: Seq[ServiceRequest] =
        if (limitedObservations) Nil
        else requests
    }
  }
  def makeTranslationModels(directRecords: Seq[BootRecord],
                            requests: Seq[ServiceRequest],
                            witnessRecords: Seq[BootRecord],
                            baseLearner: Classifier): Map[Client,MlrModel] = {

    val directStereotypeObs: Map[Trustee,List[Any]] =
      (requests.map(x => x.provider.asInstanceOf[Trustee] -> makeRequestTranslation(x).toList) ++
      directRecords.flatMap(x => x.observations)).toMap

    witnessRecords.groupBy(
      _.service.request.client
    ).mapValues(
      wrs => {
        val witnessStereotypeObs: Map[Trustee,List[Any]] = wrs.flatMap(x => x.observations).toMap
        val numClasses = makeRecordTranslation(wrs.head).size
        val rows: Iterable[Seq[Any]] = directStereotypeObs.filterKeys(te =>
          witnessStereotypeObs.contains(te)
        ).map(t =>
          witnessStereotypeObs(t._1) ++ t._2
        )

        if (rows.isEmpty) None
        else {
//          println("obssize: " + directStereotypeObs.size, witnessStereotypeObs.size, rows.size)
          val translateLearner: BR = new meka.classifiers.multilabel.BR
          translateLearner.setClassifier(new NaiveBayes())
          Some(makeTranslationModel(rows, numClasses, translateLearner))
        }
      }
    ).filterNot(_._2.isEmpty).mapValues(_.get)
  }

  def makeTranslationModel(rows: Iterable[Seq[Any]],
                           numClasses: Int,
                           baseModel: Classifier): MlrModel = {
    val directAttVals: Iterable[mutable.Map[Any, Double]] = List.fill(rows.head.size)(mutable.Map[Any, Double]("true" -> 1.0, "false" -> 0.0))
    //    println(directAttVals)
    val doubleRows = convertRowsToDouble(rows, directAttVals, classIndex = -1, discreteClass = true)
    val atts = makeAtts(rows.head, directAttVals, classIndex = -1, discreteClass = true)
    val train = makeInstances(atts, doubleRows)
    train.setClassIndex(numClasses)
    val directModel = AbstractClassifier.makeCopy(baseModel)
//    println(train)
    //    println(directModel.getClass)
    directModel.buildClassifier(train)
//        println(train)
    new MlrModel(directModel, train, directAttVals)
  }

  def translate(row: Seq[Any], model: MlrModel): Seq[Any] = {
    val query = convertRowToInstance(List.fill(model.train.classIndex())("false")++row, model.attVals, model.train)
    //    println(model.train)
    //    println("OQ "+row, model.model.distributionForInstance(query).toList)
    val ret = model.model.distributionForInstance(query).map(r => (r > 0.5).toString).toList
    //    println("TQ "+ret)
    ret
  }

  def makeRecordTranslation(record: BootRecord): Seq[Any] = {
    //    println(record.truster.id, record.trustee.id, adverts(record.service.request))
    adverts(record.service.request)
  }

  def makeRequestTranslation(request: ServiceRequest): Seq[Any] = {
    //    println("\t", request.client.id, request.provider.id, adverts(request))
    adverts(request)
  }

  def stereotypeTrainRow(record: BootRecord, labels: Map[Provider, Double]): Seq[Any] = {
    labels.getOrElse(record.trustee, record.rating) :: adverts(record.service.request)
  }

  def stereotypeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    0d :: adverts(request)
  }

}
