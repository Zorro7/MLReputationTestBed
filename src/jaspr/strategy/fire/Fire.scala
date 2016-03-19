package jaspr.strategy.fire

import jaspr.core.service.{TrustAssessment, ServiceRequest}
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.{CompositionStrategy, RatingStrategy, RatingStrategyInit}


class Fire extends RatingStrategy with CompositionStrategy with Exploration {

  override val explorationProbability: Double = 0.2

  def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[RatingStrategyInit]
    val direct = init.directRecords.withFilter(_.provider == request.provider).map(_.rating)
    val witness = init.witnessRecords.filter(_.provider == request.provider).map(_.rating)
    val result = direct.sum / (direct.size+1) + witness.sum / (witness.size+1)
    new TrustAssessment(request, result)
  }
}
