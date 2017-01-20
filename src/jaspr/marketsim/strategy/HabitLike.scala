package jaspr.marketsim.strategy

import jaspr.core.agent.Client
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.StrategyInit
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.Chooser
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.{AbstractClassifier, Classifier}
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.trees.{J48, RandomForest}

import scala.collection.mutable

/**
  * Created by phil on 20/01/17.
  */
class HabitLike(val witnessWeight: Double = 2d,
                val baseLearner: Classifier,
                override val numBins: Int,
                override val lower: Double,
                override val upper: Double) extends StrategyCore with MlrCore with StereotypeCore with ContextCore {

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
    val init = baseInit.asInstanceOf[HabitLikeInit]

    (init.directModel, init.witnessModels, init.translationModels) match {
      case (None, None, None) =>
        new TrustAssessment(baseInit.context, request, Chooser.randomDouble(0d, 1d))

      case (Some(directModel), None, None) =>
        val row = makeTestRow(init, request)
        val query = convertRowToInstance(row, directModel.attVals, directModel.train)

        val directResult = makePrediction(query, directModel)
        new TrustAssessment(baseInit.context, request, directResult)

      case (None, Some(witnessModels), None) =>
        val witnessResults = witnessModels.values.map(m => {
          val witnessRow = makeTestRow(init, request)
          val witnessQuery = convertRowToInstance(witnessRow, m.attVals, m.train)
          makePrediction(witnessQuery, m)
        })
        new TrustAssessment(baseInit.context, request, getCombinedOpinions(0, witnessResults, 1)) // witnessWeight=1 because there is no direct information

      case (Some(directModel), Some(witnessModels), None) => // This code assumes that this case applies when witnessWeight == 0
        val directRow = makeTestRow(init, request)
        val directQuery = convertRowToInstance(directRow, directModel.attVals, directModel.train)
        val directResult = makePrediction(directQuery, directModel)

        val witnessResults = witnessModels.values.map(m => {
          val witnessRow = makeTestRow(init, request)
          val witnessQuery = convertRowToInstance(witnessRow, m.attVals, m.train)
          makePrediction(witnessQuery, m)
        })
        new TrustAssessment(baseInit.context, request, getCombinedOpinions(directResult, witnessResults, witnessWeight))

      case (Some(directModel), Some(witnessModels), Some(translationModels)) =>
        val directRow = makeTestRow(init, request)
        val directQuery = convertRowToInstance(directRow, directModel.attVals, directModel.train)
        val directResult = makePrediction(directQuery, directModel)

        val witnessResults = witnessModels.map(wm => {
          val witnessRow = makeTestRow(init, request)
          val witnessQuery = convertRowToInstance(witnessRow, wm._2.attVals, wm._2.train)
          val raw = makePrediction(witnessQuery, wm._2)
          val translationRow = 0 :: raw :: Nil
          val translationModel = translationModels(wm._1)
          val inst = convertRowToInstance(translationRow, translationModel.attVals, translationModel.train)
          makePrediction(inst, translationModel, discreteClass = translationDiscrete)
          raw
        })

        new TrustAssessment(baseInit.context, request, getCombinedOpinions(directResult, witnessResults, witnessWeight))
    }
  }

  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val directRecords = getDirectRecords(network, context)
    val witnessRecords = getWitnessRecords(network, context)

    if (witnessRecords.isEmpty && directRecords.isEmpty) new HabitLikeInit(context, None, None, None)
    else if (directRecords.nonEmpty) {
      val directModel: MlrModel = makeMlrsModel(directRecords, baseLearner, makeTrainRow)
//      new HabitLikeInit(context, Some(directModel), None, None)
//    } else if (witnessRecords.nonEmpty) {
      val witnessModels: Map[Client, MlrModel] = makeOpinions(witnessRecords, r => r.service.request.client)
      new HabitLikeInit(context, Some(directModel), Some(witnessModels), None)
    } else {
      val directModel: MlrModel = makeMlrsModel(directRecords, baseLearner, makeTrainRow)
      val witnessModels: Map[Client, MlrModel] = makeOpinions(witnessRecords, r => r.service.request.client)
      val reinterpretationModels: Map[Client, MlrModel] =
        makeTranslationModels(directRecords, witnessRecords, directModel, witnessModels, r => r.service.request.client)

      new HabitLikeInit(context, Some(directModel), Some(witnessModels), Some(reinterpretationModels))
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



  val translationDiscrete: Boolean = true
  val baseTranslationLearner = baseLearner
  def makeTranslationModel[T <: Record](directRecords: Seq[T],
                                        witnessRecords: Seq[T],
                                        directModel: MlrModel,
                                        witnessModel: MlrModel,
                                        makeTranslationRow: (T,MlrModel,MlrModel) => Seq[Any]): MlrModel = {
    val reinterpretationRows: Seq[Seq[Any]] =
      directRecords.map(record => makeTranslationRow(record, witnessModel, directModel)) ++
        witnessRecords.map(record => makeTranslationRow(record, witnessModel, directModel))
    val reinterpretationAttVals: Iterable[mutable.Map[Any, Double]] = List.fill(reinterpretationRows.head.size)(mutable.Map[Any, Double]())
    val doubleRows = convertRowsToDouble(reinterpretationRows, reinterpretationAttVals, classIndex, discreteClass = translationDiscrete)
    val atts = makeAtts(reinterpretationRows.head, reinterpretationAttVals, classIndex, discreteClass = translationDiscrete)
    val reinterpretationTrain = makeInstances(atts, doubleRows)
    reinterpretationTrain.setClassIndex(classIndex)
    val reinterpretationModel = AbstractClassifier.makeCopy(baseTranslationLearner)
    reinterpretationModel.buildClassifier(reinterpretationTrain)
    new MlrModel(reinterpretationModel, reinterpretationTrain, reinterpretationAttVals)
  }

  def makeTranslationRow(record: Record with ServiceRecord with RatingRecord,
                         fromModel: MlrModel,
                         toModel: MlrModel): Seq[Any] = {
    val fromRow = makeTrainRow(record)
    val fromQuery = convertRowToInstance(fromRow, fromModel.attVals, fromModel.train)
    val toRow = makeTrainRow(record)
    val toQuery = convertRowToInstance(toRow, toModel.attVals, toModel.train)
    (if (translationDiscrete) makePrediction(toQuery, toModel, discreteClass = false)
    else makePrediction(toQuery, toModel)) ::
      makePrediction(fromQuery, fromModel) ::
      Nil
  }

  def makeTranslationModels[K1](directRecords: Seq[ServiceRecord with RatingRecord],
                                   witnessRecords: Seq[ServiceRecord with RatingRecord],
                                   directModel: MlrModel,
                                   witnessModels: Map[K1,MlrModel],
                                   grouping1: ServiceRecord with RatingRecord => K1): Map[K1,MlrModel] = {
    witnessRecords.groupBy(
      grouping1
    ).map(
      wrs => wrs._1 -> makeTranslationModel(directRecords, wrs._2, directModel, witnessModels(wrs._1), makeTranslationRow)
    )
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


