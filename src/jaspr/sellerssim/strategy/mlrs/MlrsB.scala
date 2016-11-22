package jaspr.sellerssim.strategy.mlrs

import jaspr.core.agent.{Client, Provider}
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, Strategy, StrategyInit}
import jaspr.strategy.CompositionStrategy
import jaspr.strategy.betareputation.Travos
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.Chooser
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.trees.RandomForest
import weka.classifiers.{AbstractClassifier, Classifier}

import scala.collection.mutable


/**
  * Created by phil on 24/03/16.
  */
class MlrsB(val baseLearner: Classifier,
            override val numBins: Int,
            val backupKind: String = "round",
            val backupThreshold: Double = 0.5,
            val witnessWeight: Double = 0.5d,
            val reinterpretationContext: Boolean = true,
            val useAdverts: Boolean = true
           ) extends CompositionStrategy with Exploration with MlrCore {

  val numFolds: Int = 5

  class Mlrs2Init(
                   context: ClientContext,
                   val trustModel: Option[MlrModel],
                   val reinterpretationModels: Option[Map[Client, MlrModel]],
                   val backupStrategyInit: Option[StrategyInit]
                 ) extends StrategyInit(context)

  override val name = this.getClass.getSimpleName + "2-" + baseLearner.getClass.getSimpleName + "-" + backupKind + "-" + backupThreshold + "-" + witnessWeight + "-" + reinterpretationContext + "-" + useAdverts

  override val explorationProbability: Double = 0.1

  baseLearner match {
    case x: NaiveBayes => x.setUseSupervisedDiscretization(true)
    case x: RandomForest =>
      x.setNumFeatures(100)
    case _ => // do nothing
  }

  //  val baseTrustModel = AbstractClassifier.makeCopy(baseLearner)
  val baseTrustModel = new MultiRegression
  baseTrustModel.setClassifier(AbstractClassifier.makeCopy(baseLearner))
  baseTrustModel.setSplitAttIndex(1)
  val baseReinterpretationModel = AbstractClassifier.makeCopy(baseLearner)

  val backupStrategy: Strategy = new Travos(lower+(upper-lower)/2d)

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[Mlrs2Init]

    (init.trustModel, init.reinterpretationModels, init.backupStrategyInit) match {
      case (None, None, None) =>
        //        println(init.context.round, "NOTHONG")
        new TrustAssessment(baseInit.context, request, Chooser.randomDouble(0d, 1d))

      case (None, None, Some(backupInit)) =>
        //        println(init.context.round, "backup")
        backupStrategy.computeAssessment(backupInit, request)

      case (Some(model), None, None) =>
        //        println(init.context.round, "as is")
        val row = makeTestRow(request)
        val query = convertRowToInstance(row, model.attVals, model.train)
        val result = makePrediction(query, model)
        new TrustAssessment(baseInit.context, request, result)

      case (Some(model), Some(reinterpretation), None) =>
        //        println(init.context.round, "reintererrtpret")
        val row = makeTestRow(request)
        val query = convertRowToInstance(row, model.attVals, model.train)
        val directResult = makePrediction(query, model)
        val witnessResults =
          for ((witness, reint) <- reinterpretation.filter(_._1 != request.client)) yield {
            val row = makeTestRow(request, witness)
            val query = convertRowToInstance(row, model.attVals, model.train)
            val reinterpretationRow = 0 :: makePrediction(query, model) :: makeReinterpretationContext(request)
            val inst = convertRowToInstance(reinterpretationRow, reint.attVals, reint.train)
            val result = makePrediction(inst, reint)
            result
          }
        val witnessResult: Double = if (witnessResults.isEmpty) 0d else witnessResults.sum / witnessResults.size.toDouble
        val score =
          if (witnessWeight < 0d || witnessWeight > 1d) directResult + witnessResults.sum
          else (1 - witnessWeight) * directResult + witnessWeight * witnessResult
        new TrustAssessment(baseInit.context, request, score)

    }
  }


  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val directRecords: Seq[Record with ServiceRecord with RatingRecord] = context.client.getProvenance[Record with ServiceRecord with RatingRecord](context.client)
    val witnessRecords: Seq[Record with ServiceRecord with RatingRecord] = network.gatherProvenance[Record with ServiceRecord with RatingRecord](context.client)
    val witnesses = context.client :: witnessRecords.map(_.service.request.client).toSet.toList
    val records = directRecords ++ witnessRecords

    def useBackup: Boolean = {
      val backupScore = backupKind match {
        case "round" => context.round
        case "auc" => crossValidate(records, baseTrustModel, makeTrainRow, numFolds)
        case "records" => records.size
        case "directRecords" => directRecords.size
        case "witnessRecords" => witnessRecords.size
      }
      backupScore < backupThreshold
    }

    if (witnessRecords.isEmpty && directRecords.isEmpty) {
      new Mlrs2Init(context, None, None, None)
    } else if (useBackup) {
      new Mlrs2Init(context, None, None, Some(backupStrategy.initStrategy(network, context, requests)))
    } else if (witnessRecords.isEmpty || directRecords.isEmpty) {
      val model = makeMlrsModel(records, baseTrustModel, makeTrainRow)
      new Mlrs2Init(context, Some(model), None, None)
    } else {
      val model = makeMlrsModel(records, baseTrustModel, makeTrainRow)
      val reinterpretationModels = witnesses.withFilter(_ != context.client).map(witness =>
        witness -> makeReinterpretationModel(directRecords, witnessRecords, context.client, witness, model)
      ).toMap

      new Mlrs2Init(context, Some(model), Some(reinterpretationModels), None)
    }

  }


  def makeReinterpretationModel(directRecords: Seq[Record with ServiceRecord with RatingRecord], witnessRecords: Seq[Record with ServiceRecord with RatingRecord], client: Client, witness: Client, model: MlrModel): MlrModel = {
    val reinterpretationRows: Seq[Seq[Any]] =
    //      directRecords.map(record => makeReinterpretationRow(record, model, witness, client)) ++
    //        witnessRecords.withFilter(_.client == witness).map(record => makeReinterpretationRow(record, model, witness, client))
    //      directRecords.map(record => makeClientReinterpretationRow(record, model, witness)) ++
    //        witnessRecords.map(record => makeWitnessReinterpretationRow(record, model, client))
    //    directRecords.map(record => makeReinterpretationRow(record, model, witness, client)) ++
    //      witnessRecords.withFilter(_.client == witness).map(record => makeReinterpretationRow(record, model, witness, client))
      directRecords.map(record => makeClientReinterpretationRow(record, model, witness)) ++
        witnessRecords.map(record => makeWitnessReinterpretationRow(record, model, client))

    val reinterpretationAttVals: Iterable[mutable.Map[Any, Double]] = List.fill(reinterpretationRows.head.size)(mutable.Map[Any, Double]())
    val doubleRows = convertRowsToDouble(reinterpretationRows, reinterpretationAttVals, classIndex)
    val atts = makeAtts(reinterpretationRows.head, reinterpretationAttVals, classIndex)
    val reinterpretationTrain = makeInstances(atts, doubleRows)
    reinterpretationTrain.setClassIndex(classIndex)
    val reinterpretationModel = AbstractClassifier.makeCopy(baseReinterpretationModel)
    reinterpretationModel.buildClassifier(reinterpretationTrain)
    //    println(client, witness, reinterpretationTrain, reinterpretationModel)
    new MlrModel(reinterpretationModel, reinterpretationTrain, reinterpretationAttVals)
  }

  def makeClientReinterpretationRow(record: Record with ServiceRecord with RatingRecord, trustModel: MlrModel, fromPOV: Client): Seq[Any] = {
    val row = makeTestRow(record, fromPOV)
    val query = convertRowToInstance(row, trustModel.attVals, trustModel.train)
    (if (discreteClass) discretizeDouble(record.rating) else record.rating) ::
      makePrediction(query, trustModel) ::
      makeReinterpretationContext(record)
  }

  def makeWitnessReinterpretationRow(record: Record with ServiceRecord with RatingRecord, trustModel: MlrModel, toPOV: Client): Seq[Any] = {
    val row = makeTestRow(record, toPOV)
    val query = convertRowToInstance(row, trustModel.attVals, trustModel.train)
    makePrediction(query, trustModel, false) ::
      record.rating ::
      makeReinterpretationContext(record)
  }

  def makeReinterpretationRow(record: Record with ServiceRecord with RatingRecord, trustModel: MlrModel, fromPOV: Client, toPOV: Client): Seq[Any] = {
    val fromRow = makeTestRow(record, fromPOV)
    val fromQuery = convertRowToInstance(fromRow, trustModel.attVals, trustModel.train)
    val toRow = makeTestRow(record, toPOV)
    val toQuery = convertRowToInstance(toRow, trustModel.attVals, trustModel.train)
    makePrediction(toQuery, trustModel, false) ::
      makePrediction(fromQuery, trustModel) ::
      makeReinterpretationContext(record)
  }

  def makeReinterpretationContext(record: Record with ServiceRecord with RatingRecord): List[Any] = {
    if (reinterpretationContext) {
      record.service.payload.name ::
        record.service.request.provider.name ::
        Nil
      //        adverts(record.provider)
    } else {
      Nil
    }

  }

  def makeReinterpretationContext(request: ServiceRequest): List[Any] = {
    if (reinterpretationContext) {
      request.payload.name ::
        request.provider.name ::
        Nil
      //        adverts(request.provider)
    } else {
      Nil
    }
  }

  def makeTrainRow(record: Record with ServiceRecord with RatingRecord): Seq[Any] = {
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.service.request.client.name ::
      record.service.request.payload.name :: // service identifier (client context)
      //      record.service.request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
      adverts(record.service.request.provider)
  }

  def makeTestRow(request: ServiceRequest, witness: Client): Seq[Any] = {
    0 ::
      witness.name ::
      request.payload.name ::
      //      request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
      adverts(request.provider)
  }

  def makeTestRow(request: ServiceRequest): Seq[Any] = {
    0 ::
      request.client.name ::
      request.payload.name ::
      //      request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
      adverts(request.provider)
  }

  def makeTestRow(record: Record with ServiceRecord with RatingRecord, witness: Client): Seq[Any] = {
    0 ::
      witness.name ::
      record.service.request.payload.name :: // service identifier (client context)
      //            record.service.request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
      adverts(record.service.request.provider)
  }

  def adverts(provider: Provider): List[Any] = {
    if (useAdverts) {
      provider.name :: provider.generalAdverts.values.map(_.value).toList
    } else {
      provider.name :: Nil
    }
  }
}
