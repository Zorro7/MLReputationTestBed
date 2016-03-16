package jaspr.simplesim.strategy

import jaspr.core.Network
import jaspr.core.service.{TrustAssessment, ServiceRequest, ClientContext}
import jaspr.core.strategy.{StrategyInit, Strategy}
import jaspr.utilities.Chooser

/**
 * Created by phil on 16/03/16.
 */
class NoStrategy extends Strategy {

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    new StrategyInit
  }

  override def computeAssessment(init: StrategyInit, request: ServiceRequest): TrustAssessment = {
    new TrustAssessment(request, Chooser.randomDouble(0,1))
  }

  override def select(orderedAssessments: Seq[TrustAssessment]): TrustAssessment = {
    orderedAssessments.head
  }

  override def possibleRequests(network: Network, context: ClientContext): Seq[ServiceRequest] = {
    network.providers.map(new ServiceRequest(context.client, _, context.round, 1,
      context.properties
    ))
  }
}
