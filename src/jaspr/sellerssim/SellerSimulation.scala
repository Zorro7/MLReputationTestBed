package jaspr.sellerssim

import jaspr.core.results.Result
import jaspr.core.simulation.{Network, NetworkMarket, Simulation}
import jaspr.sellerssim.staticsimulation.StaticSellerMultiConfiguration
import jaspr.utilities.{Chooser, Tickable}

/**
  * Created by phil on 21/03/16.
  */
object SellerSimulation extends App {
  val results = Simulation(new StaticSellerMultiConfiguration)
}

class SellerSimulation(val config: SellerConfiguration) extends Simulation {

  override val network: Network with NetworkMarket = config.network(this)

  override def act(): Result = {
    if (round % 100 == 0) println(".")
    else print(".")

    for (client <- network.clients) {
      Chooser.ifHappens(config.clientInvolvementLikelihood)(
        client.tick()
      )()
    }

    for (provider <- network.providers) {
      provider.tick()
    }

    new Result(this)
  }

}
