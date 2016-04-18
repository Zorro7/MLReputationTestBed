package jaspr.strategy.betareputation

import jaspr.core.service.{ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.{CompositionStrategy, RatingStrategy, RatingStrategyInit}

/**
 * Created by phil on 19/03/16.
 */
class BetaReputation extends RatingStrategy with CompositionStrategy with Exploration with BetaCore {
  override val explorationProbability: Double = 0.1
  val eps: Double = 0.1
  val confidenceThreshold: Double = 1d

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[RatingStrategyInit]

    val interactionTrust = makeBetaDistribution(init.directRecords.filter(_.provider == request.provider).map(_.success))
    val interactionConfidence: Double =
      interactionTrust.integrate(interactionTrust.expected - eps, interactionTrust.expected + eps)

    val opinions =
      if (interactionConfidence < confidenceThreshold) // if interaction trust confidence is low, use witness opinions
        makeWitnessBetaDistribution(
          init.witnessRecords.filter(x => x.provider == request.provider)
        )
      else Map()

    val combinedOpinions = getCombinedOpinions(interactionTrust, opinions.values)

    val overallTrustValue = combinedOpinions.expected()

    new TrustAssessment(baseInit.context, request, overallTrustValue)
  }
}
