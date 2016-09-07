package jaspr.sellerssim.staticsimulation

import jaspr.core.agent.{Client, Provider}
import jaspr.sellerssim.{SellerNetwork, SellerSimulation}
import jaspr.sellerssim.agent.{Buyer, Seller}

/**
  * Created by phil on 21/03/16.
  */
class StaticSellerNetwork(override val simulation: SellerSimulation) extends SellerNetwork {

  override val clients: Seq[Client] = List.fill(simulation.config.numClients)(
    new Buyer(simulation)
  )

  override val providers: Seq[Provider] = List.fill(simulation.config.numProviders)(
    new Seller(simulation)
  )

}
