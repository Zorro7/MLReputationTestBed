package jaspr.strategy.betareputation

import jaspr.core.service.{ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.{CompositionStrategy, RatingStrategy, RatingStrategyInit}

/**
  * Created by phil on 19/03/16.
  */
class BetaReputation(val witnessWeight: Double) extends RatingStrategy with CompositionStrategy with Exploration with BetaCore {
  override val explorationProbability: Double = 0.1
  val eps: Double = 0.1
  val confidenceThreshold: Double = 1d

  override val name = this.getClass.getSimpleName + "-" + witnessWeight

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[RatingStrategyInit]

    val interactionTrust = makeBetaDistribution(init.directRecords.filter(_.provider == request.provider).map(_.success))
    val interactionConfidence: Double =
      interactionTrust.integrate(interactionTrust.expected - eps, interactionTrust.expected + eps)

    val opinions =
      if (witnessWeight > 0 && interactionConfidence < confidenceThreshold) // if interaction trust confidence is low, use witness opinions
        makeWitnessBetaDistribution(
          init.witnessRecords.filter(x => x.provider == request.provider)
        )
      else Map()

    val combinedOpinions =
      if (witnessWeight == 1 || witnessWeight == 0) getCombinedOpinions(interactionTrust, opinions.values)
      else getCombinedOpinions(interactionTrust * (1-witnessWeight), opinions.values.map(_ * witnessWeight))

    val overallTrustValue = combinedOpinions.expected()

    new TrustAssessment(baseInit.context, request, overallTrustValue)
  }
}
