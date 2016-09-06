package jaspr.dynamicsim

import jaspr.core.Network
import jaspr.core.agent._
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.agent.{Buyer, Seller, SellerMarket}
import jaspr.simplesim.agent.{SimpleAgent, SimpleEvent, SimpleMarket}
import jaspr.utilities.Chooser

/**
 * Created by phil on 15/03/16.
 */
class DynamicNetwork(val simulation: SellerSimulation) extends Network {

  override def utility(): Double = agents.map(_.utility).sum

  override val clients: Seq[Buyer] = List.fill(simulation.config.numClients)(new Buyer(simulation))
  override val providers: Seq[Seller] = List.fill(simulation.config.numProviders)(new Seller(simulation))

  override val agents: Seq[Agent] = clients ++ providers

  override val markets: Seq[Market] = new SellerMarket(simulation) :: Nil

  override def events(): Seq[Event] = Nil

  override def possibleRequests(context: ClientContext): Seq[ServiceRequest] = {
    providers.map(
      new ServiceRequest(context.client, _, context.round, 1, context.payload, context.market)
    )
  }
}
