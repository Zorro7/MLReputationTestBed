package jaspr.dynamicsim

import jaspr.core.strategy.Strategy
import jaspr.core.{Configuration, MultiConfiguration, Simulation}
import jaspr.sellerssim.SellerConfiguration
import jaspr.strategy.NoStrategy
import jaspr.strategy.fire.Fire

/**
 * Created by phil on 15/03/16.
 */

class DynamicConfiguration(
                            override val strategy: Strategy
                          ) extends SellerConfiguration {
  override def newSimulation(): Simulation = {
    new DynamicSimulation(this)
  }

  override val numSimulations: Int = 10
  override val numRounds: Int = 50
  override val numClients: Int = 10
  override val numProviders: Int = 10

}


class DynamicMultiConfiguration extends MultiConfiguration {
  override val directComparison = true

  override lazy val configs: Seq[Configuration] =
    new DynamicConfiguration(new Fire) ::
    new DynamicConfiguration(new NoStrategy) ::
      Nil
}