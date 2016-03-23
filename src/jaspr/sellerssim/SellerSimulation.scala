package jaspr.sellerssim

import jaspr.core.results.Result
import jaspr.core.{Network, Simulation}

/**
 * Created by phil on 21/03/16.
 */
object SellerSimulation extends App {
  Simulation(new SellerMultiConfiguration)
}

class SellerSimulation(val config: SellerConfiguration) extends Simulation {

  override val network: Network = new SellerNetwork(this)


  override def act(): Result = {
    for (client <- network.clients) {
      client.tick()
    }

    for (provider <- network.providers) {
      provider.tick()
    }

    new Result(this)
  }

}
