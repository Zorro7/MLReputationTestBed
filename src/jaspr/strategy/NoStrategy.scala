package jaspr.strategy

import jaspr.core.Network
import jaspr.core.service.{Payload, TrustAssessment, ServiceRequest, ClientContext}
import jaspr.core.strategy.{NoExploration, StrategyInit, Strategy}
import jaspr.utilities.Chooser

/**
 * Created by phil on 16/03/16.
 */
class NoStrategy extends Strategy with NoExploration {

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    new StrategyInit
  }

  override def computeAssessment(init: StrategyInit, request: ServiceRequest): TrustAssessment = {
    println(request+" DEPENDENCIES  " +request.dependencies)
    new TrustAssessment(request, Chooser.randomDouble(0,1))
  }

}
