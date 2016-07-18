package jaspr.sellerssim.strategy.general.mlrs2

import jaspr.core.Network
import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.sellerssim.service.BuyerRecord
import jaspr.strategy.CompositionStrategy
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.{AbstractClassifier, Classifier}

import scala.collection.mutable


/**
 * Created by phil on 24/03/16.
 */
class MlrsEvents(val baseLearner: Classifier,
                override val numBins: Int,
                val witnessWeight: Double = 0.5d,
                val reinterpretationContext: Boolean = true
                  ) extends CompositionStrategy with Exploration with MlrsCore {


  class Mlrs2Init(
                   context: ClientContext,
                   val trustModel: Option[MlrsModel],
                   val reinterpretationModels: Option[Map[Client,MlrsModel]],
                   val feLikelihood: Map[String,Double]
                   ) extends StrategyInit(context)

  override val name = this.getClass.getSimpleName+"2-"+baseLearner.getClass.getSimpleName+"-"+witnessWeight+"-"+reinterpretationContext

  override val explorationProbability: Double = 0.1

  if (baseLearner.isInstanceOf[NaiveBayes]) baseLearner.asInstanceOf[NaiveBayes].setUseSupervisedDiscretization(true)

//  val baseTrustModel = AbstractClassifier.makeCopy(baseLearner)
  val baseTrustModel = new MultiRegression
  baseTrustModel.setClassifier(AbstractClassifier.makeCopy(baseLearner))
  baseTrustModel.setSplitAttIndex(1)
  val baseReinterpretationModel = AbstractClassifier.makeCopy(baseLearner)

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[Mlrs2Init]

    init.trustModel match {
      case None => new TrustAssessment(baseInit.context, request, 0d)
      case Some(model) =>
        init.reinterpretationModels match {
          case None =>
            val results = init.feLikelihood.map(event => {
              val row = makeTestRow(request, event._1)
              val query = convertRowToInstance(row, model.attVals, model.train)
              event._2 * makePrediction(query, model)
            })
            new TrustAssessment(baseInit.context, request, results.sum)
          case Some(reinterpretationModels) =>
            val directResults = init.feLikelihood.map(event => {
              val row = makeTestRow(request, event._1)
              val query = convertRowToInstance(row, model.attVals, model.train)
              event._2 * makePrediction(query, model)
            })
            val directResult = directResults.sum
            val witnessResults =
              for ((witness,reint) <- reinterpretationModels.filter(_._1 != request.client)) yield {
                val wRes = init.feLikelihood.map(event => {
                  val row = makeTestRow(request, witness, event._1)
                  val query = convertRowToInstance(row, model.attVals, model.train)
                  val reinterpretationRow = 0 :: makePrediction(query, model) :: makeReinterpretationContext(request)
                  val inst = convertRowToInstance(reinterpretationRow, reint.attVals, reint.train)
                  event._2 * makePrediction(inst, reint)
                })
                wRes.sum
              }
            val witnessResult: Double = if (witnessResults.isEmpty) 0d else witnessResults.sum / witnessResults.size.toDouble
            val score =
              if (witnessWeight < 0d || witnessWeight > 1d) directResult + witnessResults.sum
              else (1-witnessWeight)*directResult + witnessWeight*witnessResult
            new TrustAssessment(baseInit.context, request, score)
        }
    }
  }


  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords: Seq[BuyerRecord] = context.client.getProvenance[BuyerRecord](context.client)
    val witnessRecords: Seq[BuyerRecord] = network.gatherProvenance[BuyerRecord](context.client)
    val witnesses = context.client :: witnessRecords.map(_.service.request.client).toSet.toList
    val records = directRecords ++ witnessRecords

    val feLikelihood = directRecords.groupBy(_.event.name).mapValues(_.size / directRecords.size.toDouble)

    if (witnessRecords.isEmpty && directRecords.isEmpty) new Mlrs2Init(context, None, None, feLikelihood)
    else if (witnessRecords.isEmpty || directRecords.isEmpty) {
      val model = makeMlrsModel(records, baseTrustModel, makeTrainRow)
      new Mlrs2Init(context, Some(model), None, feLikelihood)
    } else {
      val model = makeMlrsModel(records, baseTrustModel, makeTrainRow)

      val reinterpretationModels = witnesses.withFilter(_ != context.client).map(witness =>
        witness -> makeReinterpretationModel(directRecords, witnessRecords, context.client, witness, model)
      ).toMap

      new Mlrs2Init(context, Some(model), Some(reinterpretationModels), feLikelihood)
    }
  }

  def makeReinterpretationModel(directRecords: Seq[BuyerRecord], witnessRecords: Seq[BuyerRecord], client: Client, witness: Client, model: MlrsModel): MlrsModel = {
    val reinterpretationRows: Seq[Seq[Any]] =
//      directRecords.map(record => makeReinterpretationRow(record, model, witness, client)) ++
//        witnessRecords.withFilter(_.client == witness).map(record => makeReinterpretationRow(record, model, witness, client))
//      directRecords.map(record => makeClientReinterpretationRow(record, model, witness)) ++
//        witnessRecords.map(record => makeWitnessReinterpretationRow(record, model, client))
//    directRecords.map(record => makeReinterpretationRow(record, model, witness, client)) ++
//      witnessRecords.withFilter(_.client == witness).map(record => makeReinterpretationRow(record, model, witness, client))
      directRecords.map(record => makeClientReinterpretationRow(record, model, witness)) ++
        witnessRecords.map(record => makeWitnessReinterpretationRow(record, model, client))

    val reinterpretationAttVals: Iterable[mutable.Map[Any,Double]] = List.fill(reinterpretationRows.head.size)(mutable.Map[Any,Double]())
    val doubleRows = convertRowsToDouble(reinterpretationRows, reinterpretationAttVals, classIndex)
    val atts = makeAtts(reinterpretationRows.head, reinterpretationAttVals, classIndex)
    val reinterpretationTrain = makeInstances(atts, doubleRows)
    reinterpretationTrain.setClassIndex(classIndex)
    val reinterpretationModel = AbstractClassifier.makeCopy(baseReinterpretationModel)
    reinterpretationModel.buildClassifier(reinterpretationTrain)
//    println(client, witness, reinterpretationTrain, reinterpretationModel)
    new MlrsModel(reinterpretationModel, reinterpretationTrain, reinterpretationAttVals)
  }

  def makeClientReinterpretationRow(record: BuyerRecord, trustModel: MlrsModel, fromPOV: Client): Seq[Any] = {
    val row = makeTestRow(record, fromPOV)
    val query = convertRowToInstance(row, trustModel.attVals, trustModel.train)
    (if (discreteClass) discretizeDouble(record.rating) else record.rating) ::
      makePrediction(query, trustModel) ::
      makeReinterpretationContext(record)
  }

  def makeWitnessReinterpretationRow(record: BuyerRecord, trustModel: MlrsModel, toPOV: Client): Seq[Any] = {
    val row = makeTestRow(record, toPOV)
    val query = convertRowToInstance(row, trustModel.attVals, trustModel.train)
    makePrediction(query, trustModel, false) ::
      record.rating ::
      makeReinterpretationContext(record)
  }

  def makeReinterpretationRow(record: BuyerRecord, trustModel: MlrsModel, fromPOV: Client, toPOV: Client): Seq[Any] = {
    val fromRow = makeTestRow(record, fromPOV)
    val fromQuery = convertRowToInstance(fromRow, trustModel.attVals, trustModel.train)
    val toRow = makeTestRow(record, toPOV)
    val toQuery = convertRowToInstance(toRow, trustModel.attVals, trustModel.train)
    makePrediction(toQuery, trustModel, false) ::
      makePrediction(fromQuery, trustModel) ::
      makeReinterpretationContext(record)
  }

  def makeReinterpretationContext(record: BuyerRecord): List[Any] = {
    if (reinterpretationContext) {
      record.payload.name ::
        record.provider.name ::
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

  def makeTrainRow(record: BuyerRecord): Seq[Any] = {
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.client.name ::
      record.service.request.payload.name :: // service identifier (client context)
      record.event.name ::
//      record.service.request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
        adverts(record.service.request.provider)
  }

  def makeTestRow(request: ServiceRequest, witness: Client, serviceContext: String): Seq[Any] = {
    0 ::
      witness.name ::
      request.payload.name ::
      serviceContext ::
//      request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
        adverts(request.provider)
  }

  def makeTestRow(request: ServiceRequest, serviceContext: String): Seq[Any] = {
    0 ::
      request.client.name ::
      request.payload.name ::
      serviceContext ::
//      request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
        adverts(request.provider)
  }

  def makeTestRow(record: BuyerRecord, witness: Client): Seq[Any] = {
    0 ::
      witness.name ::
      record.service.request.payload.name :: // service identifier (client context)
      record.event.name ::
//            record.service.request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
      adverts(record.service.request.provider)
  }

  def adverts(provider: Provider): List[Any] = {
    provider.name :: provider.advertProperties.values.map(_.value).toList
  }
}