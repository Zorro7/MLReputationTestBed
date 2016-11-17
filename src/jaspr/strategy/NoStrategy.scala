package jaspr.strategy

import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{NoExploration, Strategy, StrategyInit}
import jaspr.utilities.Chooser

/**
  * Created by phil on 16/03/16.
  */
class NoStrategy extends Strategy with NoExploration {

  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    new StrategyInit(context)
  }

  override def computeAssessment(init: StrategyInit, request: ServiceRequest): TrustAssessment = {
    new TrustAssessment(init.context, request, Chooser.randomDouble(0, 1))
  }

  override def rank(init: StrategyInit, requests: Seq[ServiceRequest]): Seq[TrustAssessment] = {
    val assessments = new TrustAssessment(init.context, Chooser.choose(requests), 1) :: Nil
    Chooser.shuffle(assessments)
  }

}
