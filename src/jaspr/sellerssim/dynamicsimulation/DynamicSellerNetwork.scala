package jaspr.sellerssim.dynamicsimulation

import jaspr.core.agent._
import jaspr.sellerssim.agent.{Buyer, Seller, SellerMarket}
import jaspr.sellerssim.{SellerNetwork, SellerSimulation}
import jaspr.utilities.Tickable

/**
  * Created by phil on 15/03/16.
  */
class DynamicSellerNetwork(override val simulation: SellerSimulation) extends SellerNetwork with Tickable {

  var _clients: Seq[Buyer] = Nil
  var _providers: Seq[Seller] = Nil
  tick()

  override def clients: Seq[Buyer] = _clients
  override def providers: Seq[Seller] = _providers

  override val market: Market = new SellerMarket

  override def tick(): Unit = {
    println("tick", simulation.round)
    _clients = List.fill(simulation.config.numClients)(
      new Buyer(simulation)
    )
    _providers = List.fill(simulation.config.numClients)(
      new Seller(simulation)
    )
  }
}
