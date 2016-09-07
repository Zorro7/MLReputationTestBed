package jaspr.strategy

import jaspr.core.service.{ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{Strategy, StrategyInit}

/**
  * Created by phil on 19/03/16.
  */
trait CompositionStrategy extends Strategy {

  override def computeAssessment(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val requestScores = request.flatten().map(x => compute(baseInit, request))
    new TrustAssessment(baseInit.context, request, requestScores.map(_.trustValue).sum)
  }

  def compute(init: StrategyInit, request: ServiceRequest): TrustAssessment
}
