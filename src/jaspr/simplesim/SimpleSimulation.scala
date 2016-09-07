package jaspr.simplesim

import jaspr.core.results.Result
import jaspr.core.simulation.{Configuration, Simulation}
import jaspr.utilities.Chooser

/**
  * Created by phil on 15/03/16.
  */
object SimpleSimulation extends App {
  Simulation(new SimpleMultiConfiguration)
}

class SimpleSimulation(override val config: Configuration) extends Simulation {

  override val network: SimpleNetwork = new SimpleNetwork(this)

  override def act(): Result = {
    for (agent <- network.clients) {
      Chooser.ifHappens(0.1)(agent.clientTick())()
    }
    for (agent <- network.providers) {
      agent.providerTick()
    }
    new Result(this)
  }
}
