package jaspr.marketsim

import jaspr.marketsim.{MarketConfiguration, MarketNetwork}
import jaspr.core.results.Result
import jaspr.core.simulation.Simulation
import jaspr.utilities.Chooser

/**
  * Created by phil on 18/01/17.
  */
class MarketSimulation(override val config: MarketConfiguration) extends Simulation {

  override val network: MarketNetwork = new MarketNetwork(this)

  override def act(): Result = {
    if (round % 100 == 0) println(".")
    else print(".")

    network.tick()

    val participatingClients =
      if (config.trustorParticipation > 1d) {
        Chooser.sample(network.clients, config.trustorParticipation.toInt)
      } else {
        network.clients.filter(x => Chooser.randomBoolean(config.trustorParticipation))
      }
    for (client <- participatingClients) {
      client.tick()
    }

    for (provider <- network.providers) {
      provider.tick()
    }

    new Result(this)
  }

}