package jaspr.strategy.stereotype

import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.sellerssim.strategy.general.SingleModelStrategy
import jaspr.strategy.betareputation.BetaCore
import jaspr.strategy.{CompositionStrategy, Rating, RatingStrategy}
import weka.classifiers.Classifier

/**
 * Created by phil on 05/07/16.
 */
class Burnett extends CompositionStrategy with Exploration with BetaCore with RatingStrategy with SingleModelStrategy {

  override val numBins: Int = 0
  override val explorationProbability: Double = 0.1
  override val baseLearner: Classifier = new weka.classifiers.trees.M5P()


  class BurnettInit(context: ClientContext,
                    trustModel: Option[MlrsModel],
                    val ratings: Seq[Rating]
                     ) extends BasicInit(context, trustModel)

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val records = getRecords(network, context)
    if (records.isEmpty) {
      new BurnettInit(context, None, Nil)
    } else {
      val trustModel = makeMlrsModel(records, baseLearner, makeTrainRow)
      new BurnettInit(
        context,
        Some(trustModel),
        toRatings(records.map(_.asInstanceOf[ServiceRecord with RatingRecord]))
      )
    }
  }

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[BurnettInit]

    val betadist = makeBetaDistribution(init.ratings.filter(_.provider == request.provider).map(_.success))
    val belief = betadist.expected()
    val uncert = betadist.uncertainty()*2 // Burnett's paper states 2/(r+s+2) where r and s are [un]successful interactions

    val stereotype = init.trustModel match {
      case None => 0.5
      case Some(trustModel) =>
        val row = makeTestRow(init, request)
        val query = convertRowToInstance(row, trustModel.attVals, trustModel.train)
        val pred = trustModel.model.classifyInstance(query)
        if (discreteClass) trustModel.train.classAttribute().value(pred.toInt).toDouble
        else pred
    }

    new TrustAssessment(init.context, request, belief + stereotype*uncert)
  }

  override def makeTrainRow(baseRecord: Record): Seq[Any] = {
    val record = baseRecord.asInstanceOf[ServiceRecord with RatingRecord]
    (if (discreteClass) discretizeInt(record.rating) else record.rating) ::
      record.service.request.provider.advertProperties.values.map(_.value).toList
  }

  def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    0 :: request.provider.advertProperties.values.map(_.value).toList
  }

  def getRecords(network: Network, context: ClientContext): Seq[Record] = {
    context.client.getProvenance(context.client) ++ network.gatherProvenance(context.client)
  }
}
