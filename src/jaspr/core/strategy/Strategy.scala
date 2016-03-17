package jaspr.core.strategy

import jaspr.core.Network
import jaspr.core.service.{ServiceRequest, TrustAssessment, ClientContext}
import jaspr.utilities.Chooser

/**
 * Created by phil on 16/03/16.
 */
abstract class Strategy {

  val name: String = this.getClass.getSimpleName
  override def toString = name

  def initStrategy(network: Network, context: ClientContext): StrategyInit
  def computeAssessment(init: StrategyInit, request: ServiceRequest): TrustAssessment

  def select(orderedAssessments: Seq[TrustAssessment]): TrustAssessment

  def assessReputation(network: Network, context: ClientContext): TrustAssessment = {
    val requests = network.possibleRequests(network, context)
    val init = initStrategy(network, context)
    val orderedProviders = rank(init, requests)
    select(orderedProviders)
  }

  def rank(init: StrategyInit, requests: Seq[ServiceRequest]): Seq[TrustAssessment] = {
    val assessments = requests.map(computeAssessment(init, _))
    Chooser.shuffle(assessments).sortBy(x =>
      if (x.trustValue.isNaN) Double.MinValue else x.trustValue
    ).reverse
  }

}
