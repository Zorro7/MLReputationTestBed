package jaspr.marketsim.strategy

import jaspr.core.agent.Client
import jaspr.core.provenance.{RatingRecord, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.StrategyInit
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.Chooser
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.Classifier
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.trees.{J48, RandomForest}

/**
  * Created by phil on 19/01/17.
  */
class BRSLike(val baseLearner: Classifier,
              override val numBins: Int,
              override val lower: Double,
              override val upper: Double) extends StrategyCore with MlrCore {

  override val name: String =
    this.getClass.getSimpleName+"-"+baseLearner.getClass.getSimpleName+":"+numBins

  baseLearner match {
    case x: NaiveBayes => x.setUseSupervisedDiscretization(true)
    case x: MultiRegression =>
      val bayes = new NaiveBayes
      bayes.setUseSupervisedDiscretization(true)
      x.setClassifier(bayes)
      x.setSplitAttIndex(-1)
    case x: J48 => x.setUnpruned(true)
    case x: RandomForest =>
      x.setNumExecutionSlots(1)
      x.setNumFeatures(100)
    case _ => // do nothing
  }

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init: BRSLikeInit = baseInit.asInstanceOf[BRSLikeInit]
    init.trustModel match {
      case None => new TrustAssessment(baseInit.context, request, Chooser.randomDouble(0,1))
      case Some(trustModel) =>
        val row = makeTestRow(init, request)
        val query = convertRowToInstance(row, trustModel.attVals, trustModel.train)
        val result = makePrediction(query, trustModel)

        new TrustAssessment(baseInit.context, request, result)
    }
  }

  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val directRecords = getDirectRecords(network, context)
    val witnessRecords = getWitnessRecords(network, context)

    if (directRecords.isEmpty && witnessRecords.isEmpty) {
      new BRSLikeInit(context, None)
    } else {
      val trustModel: MlrModel = makeMlrsModel(directRecords ++ witnessRecords, baseLearner, makeTrainRow)

      new BRSLikeInit(context, Some(trustModel))
    }
  }

  def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    0d ::
      request.provider.name ::
      Nil
  }

  def makeTrainRow(record: ServiceRecord with RatingRecord): Seq[Any] = {
    label(record) ::
      record.service.request.provider.name ::
      Nil
  }

  def label(record: RatingRecord): Any = {
    if (numBins < 1) record.rating
    else if (numBins == 2) record.success
    else discretizeInt(record.rating)
  }
}


class BRSContextLike(baseLearner: Classifier,
                     numBins: Int,
                     lower: Double,
                     upper: Double) extends BRSLike(baseLearner, numBins, lower, upper) with ContextCore {

  override def makeTrainRow(record: ServiceRecord with RatingRecord): Seq[Any] = {
    super.makeTrainRow(record) ++
      context(record.service.request.payload)
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    super.makeTestRow(init, request) ++
      context(request.payload)
  }
}

class BRSStereotypeLike(baseLearner: Classifier,
                         numBins: Int,
                         lower: Double,
                         upper: Double) extends BRSLike(baseLearner, numBins, lower, upper) with StereotypeCore {

  override def makeTrainRow(record: ServiceRecord with RatingRecord): Seq[Any] = {
    super.makeTrainRow(record) ++ record.service.request.provider.adverts.values.map(_.value.toString).toList
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    super.makeTestRow(init, request) ++ request.provider.adverts.values.map(_.value.toString).toList
  }
}

class BRSStereotypeContextLike(baseLearner: Classifier,
                                numBins: Int,
                                lower: Double,
                                upper: Double) extends BRSLike(baseLearner, numBins, lower, upper) with StereotypeCore with ContextCore {

  override def makeTrainRow(record: ServiceRecord with RatingRecord): Seq[Any] = {
    super.makeTrainRow(record) ++
      adverts(record.provider) ++
      context(record.service.request.payload)
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    super.makeTestRow(init, request) ++
      adverts(request.provider) ++
      context(request.payload)
  }
}