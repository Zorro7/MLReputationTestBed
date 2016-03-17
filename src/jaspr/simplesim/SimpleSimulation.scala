package jaspr.simplesim

import jaspr.core.agent.Client
import jaspr.core.{Network, Simulation}
import jaspr.core.results.Result

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
      agent.tick()
    }
    for (event <- network.events()) {
      event.tick()
    }
    for (agent <- network.providers) {
      agent.tick()
    }
    new Result(this)
  }
}
