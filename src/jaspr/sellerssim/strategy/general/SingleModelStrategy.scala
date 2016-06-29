package jaspr.sellerssim.strategy.general

import jaspr.core.Network
import jaspr.core.provenance.{Record, ServiceRecord, RatingRecord}
import jaspr.core.service.{TrustAssessment, ServiceRequest, ClientContext}
import jaspr.core.strategy.{StrategyInit, Exploration}
import jaspr.sellerssim.strategy.MlrsCore
import jaspr.strategy.CompositionStrategy
import weka.classifiers.Classifier
import weka.classifiers.bayes.NaiveBayes

/**
 * Created by phil on 29/06/16.
 */
trait SingleModelStrategy extends CompositionStrategy with Exploration with MlrsCore {

  override val explorationProbability: Double = 0.1
  override val numBins: Int = 10

  val baseLearner: Classifier = new NaiveBayes

  class BasicInit(context: ClientContext,
                  val trustModel: Option[MlrsModel]
                   ) extends StrategyInit(context)

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val records = getRecords(network, context)

    if (records.isEmpty) {
      new BasicInit(context, None)
    } else {
      val trustModel = makeMlrsModel(records, baseLearner, makeTrainRow)
      new BasicInit(context, Some(trustModel))
    }
  }

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init: BasicInit = baseInit.asInstanceOf[BasicInit]
    init.trustModel match {
      case None => new TrustAssessment(baseInit.context, request, 0d)
      case Some(trustModel) =>
        val row = makeTestRow(init, request)
        val query = convertRowToInstance(row, trustModel.attVals, trustModel.train)
        val pred = trustModel.model.classifyInstance(query)
        val result =
          if (discreteClass) trustModel.train.classAttribute().value(pred.toInt).toDouble
          else pred
        new TrustAssessment(baseInit.context, request, result)
    }
  }

  def getRecords(network: Network, context: ClientContext): Seq[Record]

  def makeTrainRow(record: Record): Seq[Any]

  def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any]

}
