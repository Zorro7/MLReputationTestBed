package jaspr.strategy.fire

import jaspr.core.service.{ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.{CompositionStrategy, RatingStrategy, RatingStrategyInit}

import scala.math._


class Fire(val witnessWeight: Double = 0.5) extends RatingStrategy with CompositionStrategy with Exploration {

  override val name = this.getClass.getSimpleName + "-" + witnessWeight
  override val explorationProbability: Double = 0.1

  // In recency scaling, the number of rounds before an interaction rating should be half that of the current round
  val RecencyScalingPeriodToHalf = 5
  // FIRE's recency scaling factor for interaction ratings (lambda)
  val RecencyScalingFactor = -RecencyScalingPeriodToHalf / log(0.5)

  def weightRating(ratingRound: Int, currentRound: Int): Double = {
    pow(E, -((currentRound - ratingRound) / RecencyScalingFactor))
  }

  //  override def initStrategy(network: Network, context: ClientContext) = {
  //    val x = super.initStrategy(network, context)
  //    println(x.asInstanceOf[RatingStrategyInit].directRecords.map(_.provider).distinct)
  //    x
  //  }
  def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[RatingStrategyInit]
    val direct = init.directRecords.withFilter(_.provider == request.provider).map(x => weightRating(x.round, init.context.round) * x.rating)
    val witness =
      if (witnessWeight == 0d) Nil
      else init.witnessRecords.withFilter(_.provider == request.provider).map(x => weightRating(x.round, init.context.round) * x.rating)
    val result =
      (1 - witnessWeight) * direct.sum / (direct.size + 1) +
        witnessWeight * witness.sum / (witness.size + 1)
    new TrustAssessment(baseInit.context, request, result)
  }
}
