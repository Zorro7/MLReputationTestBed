package jaspr.dynamicsim

import jaspr.core.Simulation
import jaspr.core.results.Result
import jaspr.utilities.Chooser

/**
 * Created by phil on 15/03/16.
 */
object DynamicSimulation extends App {
  Simulation(new DynamicMultiConfiguration)
}

class DynamicSimulation(override val config: DynamicConfiguration) extends Simulation {

  override val network: DynamicNetwork = new DynamicNetwork(this)

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
