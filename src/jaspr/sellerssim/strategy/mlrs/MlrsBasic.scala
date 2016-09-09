package jaspr.sellerssim.strategy.mlrs

import jaspr.core.agent.Provider
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.sellerssim.service.{BuyerRecord, ProductPayload}
import jaspr.strategy.CompositionStrategy
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.{AbstractClassifier, Classifier}

/**
  * Created by phil on 24/03/16.
  */
class MlrsBasic(val baseLearner: Classifier,
                override val numBins: Int,
                val witnessWeight: Double = 0.5d,
                val useAdvertProperties: Boolean = true
               ) extends CompositionStrategy with Exploration with MlrsCore {


  class Mlrs2Init(context: ClientContext, val trustModel: Option[MlrsModel]) extends StrategyInit(context)

  override val name = this.getClass.getSimpleName + "-" + baseLearner.getClass.getSimpleName + "-" + numBins + "-" + witnessWeight

  override val explorationProbability: Double = 0.1

  val baseModel = new MultiRegression
  baseModel.setClassifier(AbstractClassifier.makeCopy(baseLearner))
  baseModel.setSplitAttIndex(1)

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[Mlrs2Init]

    init.trustModel match {
      case None => new TrustAssessment(baseInit.context, request, 0d)
      case Some(model) =>

        val row = makeTestRow(init, request)
        val query = convertRowToInstance(row, model.attVals, model.train)
        val result =
          if (discreteClass && numBins <= 2) {
            val dist = model.model.distributionForInstance(query)
            dist.zipWithIndex.map(x => x._1 * model.train.classAttribute().value(x._2).toDouble).sum
          } else if (discreteClass) {
            val pred = model.model.classifyInstance(query)
            model.train.classAttribute().value(pred.toInt).toDouble
          } else model.model.classifyInstance(query)

        new TrustAssessment(baseInit.context, request, result)
    }
  }


  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords = context.client.getProvenance[BuyerRecord](context.client)
    val witnessRecords = network.gatherProvenance[BuyerRecord](context.client)
    val records = directRecords ++ witnessRecords

    if (records.isEmpty) new Mlrs2Init(context, None)
    else {
      val model = makeMlrsModel(records, baseModel, makeTrainRow)
      new Mlrs2Init(context, Some(model))
    }
  }


  def makeTrainRow(record: BuyerRecord): Seq[Any] = {
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.client.name ::
      record.service.request.payload.name :: // service identifier (client context)
      record.service.request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
        adverts(record.service.request.provider)
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
