package jaspr.sellerssim.dynamicsimulation

import jaspr.core.agent._
import jaspr.sellerssim.agent.{Buyer, Seller, SellerMarket}
import jaspr.sellerssim.{SellerNetwork, SellerSimulation}
import jaspr.utilities.{Chooser, Tickable}

import scala.collection.mutable

/**
  * Created by phil on 15/03/16.
  */
class DynamicSellerNetwork(override val simulation: DynamicSellerSimulation) extends SellerNetwork with Tickable {

  val config: DynamicSellerConfiguration = simulation.config.asInstanceOf[DynamicSellerConfiguration]

  private var _clients: Seq[Buyer] = List.fill(simulation.config.numClients)(
    new Buyer(simulation)
  )
  private var _providers: Seq[Seller] = List.fill(simulation.config.numClients)(
    new Seller(simulation)
  )

  private var departedClients: List[Buyer] = Nil
  private var departedProviders: List[Seller] = Nil

  override def utility(): Double = clients.map(_.utility).sum + departedClients.map(_.utility).sum

  override def clients: Seq[Buyer] = _clients
  override def providers: Seq[Seller] = _providers

  override val market: Market = new SellerMarket

  override def tick(): Unit = {
    jaspr.debug(100, "tick", simulation.round)
    _clients = clients.map(x =>
      Chooser.ifHappens(simulation.config.clientAttrition)({
        departedClients = x :: departedClients
        new Buyer(simulation)
      })(
        x
      )
    )
    _providers = providers.map(x =>
      Chooser.ifHappens(simulation.config.providerAttrition)({
        departedProviders = x :: departedProviders
        new Seller(simulation)
      })(
        x
      )
    )
  }
}
