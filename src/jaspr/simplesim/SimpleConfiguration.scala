package jaspr.simplesim

import jaspr.core.strategy.Strategy
import jaspr.core.{MultiConfiguration, Simulation, Configuration}
import jaspr.strategy.NoStrategy
import jaspr.strategy.fire.Fire

/**
 * Created by phil on 15/03/16.
 */

class SimpleConfiguration(val strategy: Strategy) extends Configuration {
  override def newSimulation(): Simulation = {
    new SimpleSimulation(this)
  }

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