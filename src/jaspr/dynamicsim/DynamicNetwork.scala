package jaspr.dynamicsim

import jaspr.core.Network
import jaspr.core.agent._
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.sellerssim.{SellerNetwork, SellerSimulation}
import jaspr.sellerssim.agent.{Buyer, Seller, SellerMarket}
import jaspr.simplesim.agent.{SimpleAgent, SimpleEvent, SimpleMarket}
import jaspr.utilities.Chooser

/**
 * Created by phil on 15/03/16.
 */
class DynamicNetwork(simulation: SellerSimulation) extends SellerNetwork(simulation) {

  override val clients: Seq[Buyer] = List.fill(simulation.config.numClients)(new Buyer(simulation))
  override val providers: Seq[Seller] = List.fill(simulation.config.numProviders)(new Seller(simulation))

  override val markets: Seq[Market] = new SellerMarket(simulation) :: Nil
  
  override def possibleRequests(context: ClientContext): Seq[ServiceRequest] = {
    providers.map(
      new ServiceRequest(context.client, _, context.round, 1, context.payload, context.market)
    )
  }
}
