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
    new StrategyInit(context)
  }

  override def computeAssessment(init: StrategyInit, request: ServiceRequest): TrustAssessment = ???

  override def rank(init: StrategyInit, requests: Seq[ServiceRequest]): Seq[TrustAssessment] = {
    val assessments = new TrustAssessment(Chooser.choose(requests), 1) :: Nil
    Chooser.shuffle(assessments).sortBy(x =>
      if (x.trustValue.isNaN) Double.MinValue else x.trustValue
    ).reverse
  }

}
