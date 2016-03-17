package jaspr.acmelogistics

import jaspr.core.{MultiConfiguration, Simulation, Configuration}
import jaspr.core.strategy.Strategy
import jaspr.strategy.{Fire, NoStrategy}

/**
 * Created by phil on 17/03/16.
 */
class ACMEConfiguration(override val strategy: Strategy) extends Configuration {

  override def newSimulation(): Simulation = new ACMESimulation(this)

  override val numSimulations: Int = 1
  override val numRounds: Int = 10

  val memoryLimit = 100

  val numClients = 1
  val numShippers = 3
  val numRefineries = 3
  val numMines = 3
}

class ACMEMultiConfiguration extends MultiConfiguration {
  override val directComparison = true

  override lazy val configs: Seq[Configuration] =
//    new ACMEConfiguration(new Fire) ::
      new ACMEConfiguration(new NoStrategy) ::
      Nil
}