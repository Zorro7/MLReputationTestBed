package jaspr.dynamicsim

import jaspr.core.agent._
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.simulation.Network
import jaspr.sellerssim.{SellerNetwork, SellerSimulation}
import jaspr.sellerssim.agent.{Buyer, Seller, SellerMarket}
import jaspr.simplesim.agent.{SimpleAgent, SimpleEvent, SimpleMarket}
import jaspr.utilities.Chooser

/**
 * Created by phil on 15/03/16.
 */
class DynamicSellerNetwork(override val simulation: SellerSimulation) extends SellerNetwork {

  override def clients: Seq[Buyer] = List.fill(simulation.config.numClients)(new Buyer(simulation))
  override def providers: Seq[Seller] = List.fill(simulation.config.numProviders)(new Seller(simulation))

  override val markets: Seq[Market] = new SellerMarket(simulation) :: Nil

  override def events(): Seq[Event] = Nil
}
