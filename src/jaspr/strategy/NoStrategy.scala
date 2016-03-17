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
    new TrustAssessment(request, Chooser.randomDouble(0,1))
  }

  override def possibleRequests(network: Network, context: ClientContext): Seq[ServiceRequest] = {
    network.providers.map(
      new ServiceRequest(context.client, _, context.round, 1, context.payload, context.market)
    )
  }
}
