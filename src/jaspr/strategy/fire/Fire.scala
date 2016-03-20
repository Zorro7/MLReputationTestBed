package jaspr.strategy.fire

import jaspr.core.service.{TrustAssessment, ServiceRequest}
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.{CompositionStrategy, RatingStrategy, RatingStrategyInit}

import scala.math._


class Fire extends RatingStrategy with CompositionStrategy with Exploration {

  override val explorationProbability: Double = 0.1

  // In recency scaling, the number of rounds before an interaction rating should be half that of the current round
  val RecencyScalingPeriodToHalf = 5
  // FIRE's recency scaling factor for interaction ratings (lambda)
  val RecencyScalingFactor = -RecencyScalingPeriodToHalf / log(0.5)

  def weightRating(ratingRound: Int, currentRound: Int): Double = {
    pow(E, -((currentRound - ratingRound) / RecencyScalingFactor))
  }

  def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[RatingStrategyInit]
    val direct = init.directRecords.withFilter(_.provider == request.provider).map(x => weightRating(x.round, init.context.round) * x.rating)
    val witness = init.witnessRecords.filter(_.provider == request.provider).map(x => weightRating(x.round, init.context.round) * x.rating)
    val result = direct.sum / (direct.size+1) + witness.sum / (witness.size+1)
    new TrustAssessment(request, result)
  }
}
