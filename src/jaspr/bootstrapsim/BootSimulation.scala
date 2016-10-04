package jaspr.bootstrapsim

import jaspr.core.results.Result
import jaspr.core.simulation.{Configuration, Network, Simulation}
import jaspr.utilities.Chooser

/**
  * Created by phil on 27/09/2016.
  */
class BootSimulation(override val config: BootConfiguration) extends Simulation {

  override val network: BootNetwork = new BootNetwork(this)

  override def act(): Result = {
    network.tick()

    for (client <- network.clients) {
      Chooser.ifHappens(config.trusterParticipationLikelihood)(
        client.tick()
      )()
    }

    for (provider <- network.providers) {
      provider.tick()
    }

    new Result(this)
  }

}