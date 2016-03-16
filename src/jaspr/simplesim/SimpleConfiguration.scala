package jaspr.simplesim

import jaspr.core.strategy.Strategy
import jaspr.core.{MultiConfiguration, Simulation, Configuration}
import jaspr.simplesim.strategy.NoStrategy

/**
 * Created by phil on 15/03/16.
 */
class SimpleConfiguration extends MultiConfiguration with Configuration {
  override def newSimulation(): Simulation = {
    new SimpleSimulation(this)
  }

  override val numSimulations: Int = 1
  override val numRounds: Int = 10
  override val strategy: Strategy = new NoStrategy()

  override lazy val configs: Seq[Configuration] =
    new SimpleConfiguration() ::
      Nil
}
