package jaspr.strategy.fire

import jaspr.core.provenance.{RatingRecord, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.{CompositionStrategy, RatingStrategy, RatingStrategyInit}

import scala.math._


class FireContext(val witnessWeight: Double = 0.5, val weightRecency: Boolean = true) extends RatingStrategy with CompositionStrategy with Exploration {

  override val name = this.getClass.getSimpleName + "-" + witnessWeight+"-"+weightRecency
  override val explorationProbability: Double = 0d

  // In recency scaling, the number of rounds before an interaction rating should be half that of the current round
  val RecencyScalingPeriodToHalf: Double = 25
  // FIRE's recency scaling factor for interaction ratings (lambda)
  val RecencyScalingFactor: Double = -RecencyScalingPeriodToHalf / log(0.5)


  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val direct = toRatings(context.client.getProvenance[ServiceRecord with RatingRecord](context.client).filter(_.service.payload.name == context.payload.name))
    val witness = toRatings(network.gatherProvenance[ServiceRecord with RatingRecord](context.client).filter(_.service.payload.name == context.payload.name))
    new RatingStrategyInit(context, direct, witness)
  }

  def weightRating(ratingRound: Int, currentRound: Int): Double = {
    if (weightRecency) {
      pow(E, -((currentRound - ratingRound) / RecencyScalingFactor))
    } else {
      1d
    }
  }


  def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[RatingStrategyInit]
    val direct = init.directRecords.withFilter(_.provider == request.provider).map(x => weightRating(x.round, init.context.round) * x.rating)
    val witness =
      if (witnessWeight == 0d) Nil
      else init.witnessRecords.withFilter(_.provider == request.provider).map(x => weightRating(x.round, init.context.round) * x.rating)
    val result =
      (1 - witnessWeight) * direct.sum / (direct.size + 1d) +
        witnessWeight * witness.sum / (witness.size + 1d)
    new TrustAssessment(baseInit.context, request, result)
  }
}
