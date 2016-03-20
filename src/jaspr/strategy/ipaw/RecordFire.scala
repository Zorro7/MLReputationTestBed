package jaspr.strategy.ipaw

import jaspr.acmelogistics.service.{SubproviderRecord, ACMERecord, GoodPayload}
import jaspr.core.Network
import jaspr.core.provenance.{RatingRecord, ServiceRecord}
import jaspr.core.service.{ClientContext, TrustAssessment, ServiceRequest}
import jaspr.core.strategy.{StrategyInit, Exploration, Strategy}
import jaspr.strategy.CompositionStrategy

import scala.math._

/**
 * Created by phil on 20/03/16.
 */

class RecordStrategyInit(context: ClientContext,
                         val directRecords: Seq[ACMERecord],
                         val witnessRecords: Seq[SubproviderRecord]) extends StrategyInit(context)

class RecordFire extends Strategy with CompositionStrategy with Exploration {
  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val direct = context.client.getProvenance(context.client)
    val witness = network.gatherProvenance(context.client)
    new RecordStrategyInit(context, direct, witness)
  }

  def meanFunch(x: RatingRecord): Double = x.rating
  def startFunch(x: ServiceRecord): Double = (x.service.start - x.service.request.start).toDouble
  def endFunch(x: ServiceRecord): Double = (x.service.end - x.service.request.end).toDouble
  def qualityFunch(x: ServiceRecord): Double =
    x.service.payload.asInstanceOf[GoodPayload].quality - x.service.request.payload.asInstanceOf[GoodPayload].quality
  def quantityFunch(x: ServiceRecord): Double =
    x.service.payload.asInstanceOf[GoodPayload].quantity - x.service.request.payload.asInstanceOf[GoodPayload].quantity

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[RecordStrategyInit]

    val direct: Double =
//      trust(request, init.directRecords, init.context.round, meanFunch)
//        trust(request, init.directRecords, init.context.round, startFunch) +
        trust(request, init.directRecords, init.context.round, endFunch) +
        trust(request, init.directRecords, init.context.round, qualityFunch) +
        trust(request, init.directRecords, init.context.round, quantityFunch)

    val witness: Double =
//          trust(request, init.witnessRecords, init.context.round, meanFunch)
//      trust(request, init.witnessRecords, init.context.round, startFunch) +
        trust(request, init.witnessRecords, init.context.round, endFunch) +
        trust(request, init.witnessRecords, init.context.round, qualityFunch) +
        trust(request, init.witnessRecords, init.context.round, quantityFunch)

    new TrustAssessment(request, direct+witness)
  }

  val interactionWeight: Double = 0.5
  val witnessWeight: Double = 0.5

  // In recency scaling, the number of rounds before an interaction rating should be half that of the current round
  val RecencyScalingPeriodToHalf = 5
  // FIRE's recency scaling factor for interaction ratings (lambda)
  val RecencyScalingFactor = -RecencyScalingPeriodToHalf / log(0.5)

  def weightRating(ratingRound: Int, currentRound: Int): Double = {
    pow(E, -((currentRound - ratingRound) / RecencyScalingFactor))
  }

  def trust(request: ServiceRequest, records: Iterable[ServiceRecord with RatingRecord],
            currentRound: Int, labelfunch: ServiceRecord with RatingRecord => Double) = {
    val tmp = records.filter(_.service.request.provider == request.provider)
    tmp.map(x =>
      labelfunch(x) * weightRating(x.service.end, currentRound)
    ).sum / (tmp.size + 1)
  }

  override val explorationProbability: Double = 0.1
}
