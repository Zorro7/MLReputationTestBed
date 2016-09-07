package jaspr.core.simulation

import jaspr.core.agent._
import jaspr.core.provenance.Record
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.utilities.Tickable

/**
  * Created by phil on 15/03/16.
  */
abstract class Network {

  val simulation: Simulation

  def utility(): Double

  def agents: Seq[Agent]

  def clients: Seq[Client]

  def providers: Seq[Provider]

  def gatherProvenance[T <: Record](agent: Agent): Seq[T] = {
    agents.withFilter(_ != agent).flatMap(_.getProvenance[T](agent))
  }

  def possibleRequests(context: ClientContext): Seq[ServiceRequest]
}

abstract class DynamicNetwork extends Network with Tickable

trait NetworkEvents {
  def events(): Seq[Event]
}

trait NetworkMarket {
  def market: Market
}

trait NetworkMarkets {
  def market: Seq[Market]
}