package jaspr.acmelogistics

import jaspr.core.results.Result
import jaspr.core.simulation.{Network, NetworkEvents, Simulation}

/**
 * Created by phil on 17/03/16.
 */
object ACMESimulation extends App {
  Simulation(new ACMEMultiConfiguration)
}

class ACMESimulation(override val config: ACMEConfiguration) extends Simulation {

  override val network: Network with NetworkEvents = new ACMENetwork(this)

  override def act(): Result = {
    for (client <- network.clients) {
      client.tick()
    }

    for (event <- network.events()) {
      event.tick()
    }

    for (provider <- network.providers) {
      provider.tick()
    }

    new Result(this)
  }

}
