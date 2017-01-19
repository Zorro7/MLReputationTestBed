package jaspr.marketsim.strategy

import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.ClientContext
import jaspr.core.simulation.Network
import jaspr.core.strategy.NoExploration
import jaspr.strategy.CompositionStrategy

/**
  * Created by phil on 18/01/2017.
  */
trait StrategyCore extends NoExploration with CompositionStrategy {

  def getDirectRecords(network: Network, context: ClientContext): Seq[ServiceRecord with RatingRecord with TrustAssessmentRecord] = {
    context.client.getProvenance[ServiceRecord with RatingRecord with TrustAssessmentRecord](context.client)
  }

  def getWitnessRecords(network: Network, context: ClientContext): Seq[ServiceRecord with RatingRecord with TrustAssessmentRecord] = {
    network.gatherProvenance[ServiceRecord with RatingRecord with TrustAssessmentRecord](context.client)
  }

  def getDirectRecords(network: Network, context: ClientContext, filter: Record => Boolean): Seq[ServiceRecord with RatingRecord with TrustAssessmentRecord] = {
    getDirectRecords(network, context).filter(filter)
  }

  def getWitnessRecords(network: Network, context: ClientContext, filter: Record => Boolean): Seq[ServiceRecord with RatingRecord with TrustAssessmentRecord] = {
    getWitnessRecords(network, context).filter(filter)
  }
}
