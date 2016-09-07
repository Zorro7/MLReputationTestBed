package jaspr.dynamicsim

import jaspr.core.agent._
import jaspr.sellerssim.agent.{Buyer, Seller, SellerMarket}
import jaspr.sellerssim.{SellerNetwork, SellerSimulation}

/**
 * Created by phil on 15/03/16.
 */
class DynamicSellerNetwork(override val simulation: SellerSimulation) extends SellerNetwork {

  override val clients: Seq[Buyer] = List.fill(simulation.config.numClients)(new Buyer(simulation))
  override val providers: Seq[Seller] = List.fill(simulation.config.numProviders)(new Seller(simulation))

  override val markets: Seq[Market] = new SellerMarket(simulation) :: Nil
}
