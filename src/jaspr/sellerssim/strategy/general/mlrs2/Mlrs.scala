package jaspr.sellerssim.strategy.general.mlrs2

import jaspr.core.Network
import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.sellerssim.service.{ProductPayload, BuyerRecord}
import jaspr.strategy.CompositionStrategy
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.functions._
import weka.classifiers.{AbstractClassifier, Classifier}
import weka.core.{DenseInstance, Attribute}

import scala.collection.JavaConversions._


/**
 * Created by phil on 24/03/16.
 */
class Mlrs(val baseLearner: Classifier,
                override val numBins: Int,
                val witnessWeight: Double = 0.5d,
                val useAdvertProperties: Boolean = true
                 ) extends CompositionStrategy with Exploration with MlrsCore {


  class Mlrs2Init(
                   context: ClientContext,
                   val trustModel: Option[MlrsModel],
                   val reinterpretationModels: Option[Map[Client,Classifier]]
                   ) extends StrategyInit(context)

  override val name = this.getClass.getSimpleName+"-"+baseLearner.getClass.getSimpleName+"-"+numBins+"-"+witnessWeight

  override val explorationProbability: Double = 0.1

  val baseModel = new MultiRegression
  baseModel.setClassifier(AbstractClassifier.makeCopy(baseLearner))
  baseModel.setSplitAttIndex(1)

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[Mlrs2Init]

    init.trustModel match {
      case None => new TrustAssessment(baseInit.context, request, 0d)
      case Some(model) =>
        init.reinterpretationModels match {
          case None =>
            val row = makeTestRow(init, request)
            val query = convertRowToInstance(row, model.attVals, model.train)
            val result = makePrediction(query, model)
            new TrustAssessment(baseInit.context, request, result)
          case Some(x) =>
            val results = for ((witness,reint) <- x.iterator.filter(_ != request.client)) yield {
              val row = makeTestRow(init, request, witness)
              val query = convertRowToInstance(row, model.attVals, model.train)
              val witResult = makePrediction(query, model)
              val result = reint.classifyInstance(new DenseInstance(1d, Array(0d, witResult)))
//              println(witResult - result)
              result
            }
            new TrustAssessment(baseInit.context, request, results.sum / results.size)
        }
    }
  }


  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords: Seq[BuyerRecord] = context.client.getProvenance[BuyerRecord](context.client)
    val witnessRecords: Seq[BuyerRecord] = network.gatherProvenance[BuyerRecord](context.client)
    val witnesses = context.client :: witnessRecords.map(_.service.request.client).toSet.toList
    val records = directRecords ++ witnessRecords

    if (witnessRecords.isEmpty && directRecords.isEmpty) new Mlrs2Init(context, None, None)
    else if (witnessRecords.isEmpty || directRecords.isEmpty) {
      val model = makeMlrsModel(records, baseModel, makeTrainRow)
      new Mlrs2Init(context, Some(model), None)
    } else {
      val model = makeMlrsModel(records, baseModel, makeTrainRow)

      val reinterpretationModels = witnesses.map(witness =>
        witness -> makeReinterpretationModel(directRecords, witnessRecords, context.client, witness, model)
      ).toMap

      new Mlrs2Init(context, Some(model), Some(reinterpretationModels))
    }
  }


//  val reinterpretationAtts =
//    new Attribute("target", discVals) ::
//      new Attribute("input", discVals) ::
//      Nil
  val reinterpretationAtts = new Attribute("target") :: new Attribute("input") :: Nil

  def makeReinterpretationModel(directRecords: Seq[BuyerRecord], witnessRecords: Seq[BuyerRecord], client: Client, witness: Client, model: MlrsModel): Classifier = {
    val reinterpretationRows: Seq[Seq[Double]] =
      directRecords.map(record =>
        record.rating :: {
          val row = makeReinterpretationRow(record, witness)
          val query = convertRowToInstance(row, model.attVals, model.train)
          makePrediction(query, model)
        } :: Nil
      ) ++ witnessRecords.filter(_.client == witness).map(record =>
        {
          val row = makeReinterpretationRow(record, client)
          val query = convertRowToInstance(row, model.attVals, model.train)
          makePrediction(query, model)
        } :: record.rating :: Nil
      )
    val reinterpretationData = makeInstances(reinterpretationAtts, reinterpretationRows)
    val reinterpretationModel = new LinearRegression
    reinterpretationModel.buildClassifier(reinterpretationData)
//    println(client, witness, reinterpretationData, reinterpretationModel)
    reinterpretationModel
  }

  def makeReinterpretationRow(record: BuyerRecord, witness: Client): Seq[Any] = {
    0 ::
      witness.name ::
      record.service.request.payload.name :: // service identifier (client context)
      record.service.request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
        adverts(record.service.request.provider)
  }

  def makeTrainRow(record: BuyerRecord): Seq[Any] = {
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.client.name ::
      record.service.request.payload.name :: // service identifier (client context)
      record.service.request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
        adverts(record.service.request.provider)
  }

  def makeTestRow(init: StrategyInit, request: ServiceRequest, witness: Client): Seq[Any] = {
    0 ::
      witness.name ::
      request.payload.name ::
      request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
        adverts(request.provider)
  }

  def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    0 ::
      request.client.name ::
      request.payload.name ::
      request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
        adverts(request.provider)
  }


  def adverts(provider: Provider): List[Any] = {
    if (useAdvertProperties) provider.name :: provider.advertProperties.values.map(_.value).toList
    else provider.name :: Nil
  }
}
