package jaspr.bootstrapsim

import jaspr.core.agent.Client
import jaspr.core.simulation.{Simulation, Configuration}
import jaspr.core.strategy.Strategy

/**
  * Created by phil on 27/09/2016.
  */
class BootConfiguration extends Configuration {
  override def newSimulation(): Simulation = ???

  override def strategy(agent: Client): Strategy = ???

  override val numSimulations: Int = 1
  override val numAgents: Int = 10
  override val numRounds: Int = 10
}
