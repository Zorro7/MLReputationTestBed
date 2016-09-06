package jaspr.dynamicsim

import jaspr.core.Network
import jaspr.core.agent._
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.dynamicsim.agent.{DynamicClient, DynamicMarket, DynamicProvider}
import jaspr.simplesim.agent.{SimpleAgent, SimpleEvent, SimpleMarket}
import jaspr.utilities.Chooser

/**
 * Created by phil on 15/03/16.
 */
class DynamicNetwork(val simulation: DynamicSimulation) extends Network {

  override def utility(): Double = agents.map(_.utility).sum

  override val clients: Seq[DynamicClient] = List.fill(simulation.config.numAgents)(new DynamicClient(simulation))
  override val providers: Seq[DynamicProvider] = List.fill(simulation.config.numAgents)(new DynamicProvider(simulation))

  override val agents: Seq[Agent] = clients ++ providers

  override val markets: Seq[Market] = new DynamicMarket(simulation) :: Nil

  override def events(): Seq[Event] = Nil

  override def possibleRequests(context: ClientContext): Seq[ServiceRequest] = {
    providers.map(
      new ServiceRequest(context.client, _, context.round, 1, context.payload, context.market)
    )
  }
}
