package jaspr.marketsim.strategy

import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, NoExploration, StrategyInit}
import jaspr.strategy.CompositionStrategy

/**
  * Created by phil on 18/01/2017.
  */
trait StrategyCore extends NoExploration with CompositionStrategy {

  def getDirectRecords(network: Network, context: ClientContext): Seq[ServiceRecord with RatingRecord] = {
    context.client.getProvenance[ServiceRecord with RatingRecord](context.client)
  }

  def getWitnessRecords(network: Network, context: ClientContext): Seq[ServiceRecord with RatingRecord] = {
    network.gatherProvenance[ServiceRecord with RatingRecord](context.client)
  }

  def getDirectRecords(network: Network, context: ClientContext, filter: Record => Boolean): Seq[ServiceRecord with RatingRecord] = {
    getDirectRecords(network, context).filter(filter)
  }

  def getWitnessRecords(network: Network, context: ClientContext, filter: Record => Boolean): Seq[ServiceRecord with RatingRecord] = {
    getWitnessRecords(network, context).filter(filter)
  }
}
