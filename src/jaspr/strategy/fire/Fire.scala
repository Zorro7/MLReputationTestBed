package jaspr.strategy.fire

import jaspr.core.agent.Provider
import jaspr.core.service.{ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.{Rating, RatingStrategy, RatingStrategyInit}


class Fire extends RatingStrategy with Exploration {

  override def computeAssessment(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init: RatingStrategyInit = baseInit.asInstanceOf[RatingStrategyInit]
    val requestScores = request.flatten().map(x => fire(x.provider, init.directRecords, init.witnessRecords))
    new TrustAssessment(request, requestScores.sum)
  }

  def fire(provider: Provider, directRecords: Seq[Rating], witnessRecords: Seq[Rating]) = {
    val direct = directRecords.withFilter(_.provider == provider).map(_.rating)
    val witness = witnessRecords.filter(_.provider == provider).map(_.rating)
    direct.sum / (direct.size+1) + witness.sum / (witness.size+1)
  }

  override val explorationProbability: Double = 0.2
}
