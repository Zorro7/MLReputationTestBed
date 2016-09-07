package jaspr.simplesim

import jaspr.core.agent.Client
import jaspr.core.simulation.{Configuration, MultiConfiguration, Simulation}
import jaspr.core.strategy.Strategy
import jaspr.strategy.NoStrategy
import jaspr.strategy.fire.Fire

/**
 * Created by phil on 15/03/16.
 */

class SimpleConfiguration(val _strategy: Strategy) extends Configuration {
  override def newSimulation(): Simulation = {
    new SimpleSimulation(this)
  }
  override def strategy(agent: Client): Strategy = _strategy

  override val numSimulations: Int = 10
  override val numRounds: Int = 50

  override val numAgents = 25

}


class SimpleMultiConfiguration extends MultiConfiguration {
  override val directComparison = true

  override lazy val configs: Seq[Configuration] =
    new SimpleConfiguration(new Fire) ::
    new SimpleConfiguration(new NoStrategy) ::
      Nil
}