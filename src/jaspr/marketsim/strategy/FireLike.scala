package jaspr.marketsim.strategy

import jaspr.core.agent.{Client, Provider}
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
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
class FireLike(val witnessWeight: Double = 2d,
               val baseLearner: Classifier,
               override val numBins: Int,
               override val lower: Double,
               override val upper: Double) extends StrategyCore with MlrCore {

  override val name: String =
    this.getClass.getSimpleName+"-"+baseLearner.getClass.getSimpleName+":"+numBins+"-"+witnessWeight

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
    val init: FireLikeInit = baseInit.asInstanceOf[FireLikeInit]

    val directResult = init.directModel match {
      case None =>
        0
      case Some(directModel) =>
        val directRow = makeTestRow(init, request)
        val directQuery = convertRowToInstance(directRow, directModel.attVals, directModel.train)
        makePrediction(directQuery, directModel)
    }

    val witnessResults = init.witnessModels.values.map(m => {
      val witnessRow = makeTestRow(init, request)
      val witnessQuery = convertRowToInstance(witnessRow, m.attVals, m.train)
      makePrediction(witnessQuery, m)
    })

    new TrustAssessment(baseInit.context, request, getCombinedOpinions(directResult, witnessResults, witnessWeight))
  }

  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val directRecords = getDirectRecords(network, context)
    val witnessRecords = getWitnessRecords(network, context)

    if (directRecords.isEmpty) {
      val witnessModels: Map[Client, MlrModel] = makeOpinions(witnessRecords, r => r.service.request.client)
      new FireLikeInit(context, None, witnessModels)
    } else {
      val directModel: MlrModel = makeMlrsModel(directRecords, baseLearner, makeTrainRow)
      val witnessModels: Map[Client, MlrModel] = makeOpinions(witnessRecords, r => r.service.request.client)
      new FireLikeInit(context, Some(directModel), witnessModels)
    }
  }

  def getCombinedOpinions(direct: Double,
                          opinions: Iterable[Double],
                          witnessWeight: Double): Double = {
    if (witnessWeight == 0) direct
    else if (witnessWeight == 1) opinions.sum
    else if (witnessWeight == 2) direct + opinions.sum
    else getCombinedOpinions(direct * (1-witnessWeight), opinions.map(_ * witnessWeight), witnessWeight = 2)
  }

  def makeOpinions[K1](records: Seq[ServiceRecord with RatingRecord],
                          grouping1: ServiceRecord with RatingRecord => K1): Map[K1,MlrModel] = {
    records.groupBy(
      grouping1
    ).map(
      rs => rs._1 -> makeMlrsModel(rs._2, baseLearner, makeTrainRow)
    )
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


class FireContextLike(witnessWeight: Double = 2d,
                      baseLearner: Classifier,
                      numBins: Int,
                      lower: Double,
                      upper: Double) extends FireLike(witnessWeight, baseLearner, numBins, lower, upper) with ContextCore {

  override def makeTrainRow(record: ServiceRecord with RatingRecord): Seq[Any] = {
    super.makeTrainRow(record) ++
      context(record.service.request.payload)
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    super.makeTestRow(init, request) ++
    context(request.payload)
  }
}

class FireStereotypeLike(witnessWeight: Double = 2d,
                      baseLearner: Classifier,
                      numBins: Int,
                      lower: Double,
                      upper: Double) extends FireLike(witnessWeight, baseLearner, numBins, lower, upper) with StereotypeCore {

  override def makeTrainRow(record: ServiceRecord with RatingRecord): Seq[Any] = {
    super.makeTrainRow(record) ++
      adverts(record.provider)
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    super.makeTestRow(init, request) ++
      adverts(request.provider)
  }
}

class FireStereotypeContextLike(witnessWeight: Double = 2d,
                         baseLearner: Classifier,
                         numBins: Int,
                         lower: Double,
                         upper: Double) extends FireLike(witnessWeight, baseLearner, numBins, lower, upper) with StereotypeCore with ContextCore {

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