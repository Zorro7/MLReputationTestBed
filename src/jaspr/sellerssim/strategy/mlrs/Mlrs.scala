package jaspr.sellerssim.strategy.mlrs

import jaspr.core.agent.{Client, Provider}
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, Payload, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.sellerssim.service.ProductPayload
import jaspr.strategy.CompositionStrategy
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.Chooser
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.functions.LinearRegression
import weka.classifiers.trees.RandomForest
import weka.classifiers.{AbstractClassifier, Classifier}

import scala.collection.mutable


/**
  * Created by phil on 24/03/16.
  */
class Mlrs(val baseLearner: Classifier,
           override val numBins: Int,
           val witnessWeight: Double = 0.5d,
           val reinterpretationContext: Boolean = true,
           val reinterpretationProvider: Boolean = true,
           val useAdverts: Boolean = true,
           val usePayloadAdverts: Boolean = true
          ) extends CompositionStrategy with Exploration with MlrCore {


  class MlrsInit(
                  context: ClientContext,
                  val trustModel: Option[MlrModel],
                  val reinterpretationModels: Option[Map[Client, MlrModel]]
                ) extends StrategyInit(context)

  override val name = {
    this.getClass.getSimpleName + "-" +
      baseLearner.getClass.getSimpleName + "-" +
      witnessWeight + "-" +
      reinterpretationContext + "-" + reinterpretationProvider+"-"+useAdverts+"-"+usePayloadAdverts
  }

  override val explorationProbability: Double = 0.1

  baseLearner match {
    case x: NaiveBayes => x.setUseSupervisedDiscretization(true)
    case x: RandomForest =>
      x.setNumTrees(25)
      x.setNumExecutionSlots(1)
      x.setNumFeatures(100)
    case _ => // do nothing
  }

  //  val baseTrustModel = AbstractClassifier.makeCopy(baseLearner)
  val baseTrustModel = new MultiRegression
  baseTrustModel.setClassifier(AbstractClassifier.makeCopy(baseLearner))
  baseTrustModel.setSplitAttIndex(1)
  val baseReinterpretationModel = new LinearRegression
  val reinterpretationDiscrete = false
//  val baseReinterpretationModel = AbstractClassifier.makeCopy(baseLearner)

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[MlrsInit]

    (init.trustModel, init.reinterpretationModels) match {
      case (None, None) =>
        new TrustAssessment(baseInit.context, request, Chooser.randomDouble(0d, 1d))

      case (Some(model), None) => // This code assumes that this case applies when witnessWeight == 0
        val row = makeTestRow(request)
        val query = convertRowToInstance(row, model.attVals, model.train)
        val result = makePrediction(query, model)
        new TrustAssessment(baseInit.context, request, result)

      case (Some(model), Some(reinterpretationModels)) =>
        val row = makeTestRow(request)
        val query = convertRowToInstance(row, model.attVals, model.train)
        val directResult = makePrediction(query, model)
        val rawWitnessResults =
          for ((witness, reint) <- reinterpretationModels.filter(_._1 != request.client)) yield {
            val row = makeTestRow(request, witness)
            val query = convertRowToInstance(row, model.attVals, model.train)
            makePrediction(query, model)
          }
        val witnessResults =
          for ((witness, reint) <- reinterpretationModels.filter(_._1 != request.client)) yield {
            val row = makeTestRow(request, witness)
            val query = convertRowToInstance(row, model.attVals, model.train)
            val reinterpretationRow = 0 :: makePrediction(query, model) :: makeReinterpretationContext(request)
            val inst = convertRowToInstance(reinterpretationRow, reint.attVals, reint.train)
            val result = makePrediction(inst, reint, discreteClass = reinterpretationDiscrete)
            result
          }
//        println(row, model.train.size, directResult, rawWitnessResults, witnessResults)
        val score =
          if (witnessWeight > 1d) (directResult + witnessResults.sum)/(witnessResults.size+1d)
          else (1 - witnessWeight) * directResult + witnessWeight * (witnessResults.sum / witnessResults.size.toDouble)
        val rawScore = (directResult + rawWitnessResults.sum)/(witnessResults.size+1d)
//        println(score, rawScore, score-rawScore)
        new TrustAssessment(baseInit.context, request, score)
    }
  }


  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val directRecords: Seq[Record with ServiceRecord with RatingRecord] = context.client.getProvenance[Record with ServiceRecord with RatingRecord](context.client)
    val witnessRecords: Seq[Record with ServiceRecord with RatingRecord] =
      if (witnessWeight == 0) Nil
      else network.gatherProvenance[Record with ServiceRecord with RatingRecord](context.client)
    val witnesses = context.client :: witnessRecords.map(_.service.request.client).toSet.toList
    val records = directRecords ++ witnessRecords

    if (witnessRecords.isEmpty && directRecords.isEmpty) new MlrsInit(context, None, None)
    else if (witnessRecords.isEmpty || directRecords.isEmpty) {
      val model = makeMlrsModel(records, baseTrustModel, makeTrainRow)
      new MlrsInit(context, Some(model), None)
    } else {
      val model = makeMlrsModel(records, baseTrustModel, makeTrainRow)

      val reinterpretationModels = witnesses.withFilter(_ != context.client).map(witness =>
        witness -> makeReinterpretationModel(directRecords, witnessRecords, context.client, witness, model)
      ).toMap

      new MlrsInit(context, Some(model), Some(reinterpretationModels))
    }
  }

  def makeReinterpretationModel(directRecords: Seq[Record with ServiceRecord with RatingRecord], witnessRecords: Seq[Record with ServiceRecord with RatingRecord], client: Client, witness: Client, model: MlrModel): MlrModel = {
    val reinterpretationRows: Seq[Seq[Any]] =
        directRecords.map(record => makeReinterpretationRow(record, model, witness, client)) ++
          witnessRecords.map(record => makeReinterpretationRow(record, model, witness, client))
//    val reinterpretationRows: Seq[Seq[Any]] = directRecords.map(record => makeReinterpretationRow(record, model, witness))
    val reinterpretationAttVals: Iterable[mutable.Map[Any, Double]] = List.fill(reinterpretationRows.head.size)(mutable.Map[Any, Double]())
    val doubleRows = convertRowsToDouble(reinterpretationRows, reinterpretationAttVals, classIndex, discreteClass = reinterpretationDiscrete)
    val atts = makeAtts(reinterpretationRows.head, reinterpretationAttVals, classIndex, discreteClass = reinterpretationDiscrete)
    val reinterpretationTrain = makeInstances(atts, doubleRows)
    reinterpretationTrain.setClassIndex(classIndex)
//    println(atts, doubleRows)
    val reinterpretationModel = AbstractClassifier.makeCopy(baseReinterpretationModel)
    reinterpretationModel.buildClassifier(reinterpretationTrain)
//        println(client, witness, reinterpretationTrain, reinterpretationModel)
    new MlrModel(reinterpretationModel, reinterpretationTrain, reinterpretationAttVals)
  }

  def makeReinterpretationRow(record: Record with ServiceRecord with RatingRecord, trustModel: MlrModel, fromPOV: Client): Seq[Any] = {
    val fromRow = makeTestRow(record, fromPOV)
    val fromQuery = convertRowToInstance(fromRow, trustModel.attVals, trustModel.train)
//    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
    record.rating ::
      makePrediction(fromQuery, trustModel) ::
      makeReinterpretationContext(record)
  }

  def makeReinterpretationRow(record: Record with ServiceRecord with RatingRecord, trustModel: MlrModel, fromPOV: Client, toPOV: Client): Seq[Any] = {
    val fromRow = makeTestRow(record, fromPOV)
    val fromQuery = convertRowToInstance(fromRow, trustModel.attVals, trustModel.train)
    val toRow = makeTestRow(record, toPOV)
    val toQuery = convertRowToInstance(toRow, trustModel.attVals, trustModel.train)
//    println(makePrediction(toQuery, trustModel, false), makePrediction(toQuery, trustModel, true, 5), makePrediction(toQuery, trustModel))
//    println(makePrediction(toQuery, trustModel, true, 5).toInt ::
//      makePrediction(fromQuery, trustModel) ::
//      makeReinterpretationContext(record))
    (if (reinterpretationDiscrete) makePrediction(toQuery, trustModel, discreteClass = false)
    else makePrediction(toQuery, trustModel)) ::
      makePrediction(fromQuery, trustModel) ::
      makeReinterpretationContext(record)
  }

  def makeReinterpretationContext(record: Record with ServiceRecord with RatingRecord): List[Any] = {
    if (reinterpretationProvider && reinterpretationContext) record.service.request.provider.name :: payload(record.service.request.payload)
    else if (reinterpretationProvider) record.service.request.provider.name :: Nil
    else if (reinterpretationContext) payload(record.service.request.payload)
    else Nil
  }

  def makeReinterpretationContext(request: ServiceRequest): List[Any] = {
    if (reinterpretationProvider && reinterpretationContext) request.provider.name :: payload(request.payload)
    else if (reinterpretationProvider) request.provider.name :: Nil
    else if (reinterpretationContext) payload(request.payload)
    else Nil
  }

  def makeTrainRow(record: Record with ServiceRecord with RatingRecord): Seq[Any] = {
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.service.request.client.name ::
      payload(record.service.request.payload) ++
//      record.service.request.payload.name :: // service identifier (client context)
      //      record.service.request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
      adverts(record.service.request)
  }

  def makeTestRow(request: ServiceRequest, witness: Client): Seq[Any] = {
    0 ::
      witness.name ::
      payload(request.payload) ++
//      request.payload.name ::
      //      request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
      adverts(request)
  }

  def makeTestRow(request: ServiceRequest): Seq[Any] = {
    0 ::
      request.client.name ::
      payload(request.payload) ++
//      request.payload.name ::
      //      request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
      adverts(request)
  }

  def makeTestRow(record: Record with ServiceRecord with RatingRecord, witness: Client): Seq[Any] = {
    0 ::
      witness.name ::
      payload(record.service.request.payload) ++
//      record.service.request.payload.name :: // service identifier (client context)
      //            record.service.request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
      adverts(record.service.request)
  }

  def payload(payload: Payload): List[Any] = {
    payload.name :: Nil
  }

  def adverts(request: ServiceRequest): List[Any] = {
    if (useAdverts && usePayloadAdverts) {
      request.provider.name :: request.provider.payloadAdverts(request.payload).values.map(_.value).toList
    } else if (useAdverts) {
      request.provider.name :: request.provider.generalAdverts.values.map(_.value).toList
    } else {
      request.provider.name :: Nil
    }
  }
}
