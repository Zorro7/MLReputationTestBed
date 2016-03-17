package jaspr.simplesim

import jaspr.core.{Simulation}
import jaspr.core.results.Result
import jaspr.utilities.Chooser

/**
 * Created by phil on 15/03/16.
 */
object SimpleSimulation extends App {
  Simulation(new SimpleMultiConfiguration)
}

class SimpleSimulation(override val config: SimpleConfiguration) extends Simulation {

  override val network: SimpleNetwork = new SimpleNetwork(this)

  override def act(): Result = {
    for (agent <- network.clients) {
      Chooser.ifHappens(0.1)(agent.clientTick())()
    }
    for (event <- network.events()) {
      event.tick()
    }
    for (agent <- network.providers) {
      agent.providerTick()
    }
    new Result(this)
  }
}
