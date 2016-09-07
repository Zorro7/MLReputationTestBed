package jaspr.simplesim

import jaspr.core.agent._
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.simulation.Network
import jaspr.simplesim.agent.SimpleAgent

/**
  * Created by phil on 15/03/16.
  */
class SimpleNetwork(val simulation: SimpleSimulation) extends Network {

  override def utility(): Double = agents.map(_.utility).sum

  override val agents: Seq[Agent] = List.fill(simulation.config.numAgents)(new SimpleAgent(simulation))

  override val clients: Seq[SimpleAgent] = agents.map(_.asInstanceOf[SimpleAgent])
  override val providers: Seq[SimpleAgent] = agents.map(_.asInstanceOf[SimpleAgent])

  override def possibleRequests(context: ClientContext): Seq[ServiceRequest] = {
    providers.map(
      new ServiceRequest(context.client, _, context.round, 1, context.payload, context.market)
    )
  }
}
