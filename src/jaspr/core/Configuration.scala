package jaspr.core

import jaspr.utilities.Chooser

/**
 * Created by phil on 15/03/16.
 */
abstract class Configuration {

  def newSimulation(): Simulation

  val numSimulations: Int
  val numRounds: Int
}

abstract class MultiConfiguration {

  val directComparison: Boolean = true
  val _seed = Chooser.randomInt(0, Int.MaxValue)
  def seed(configIndex: Int, simulationIndex: Int) = {
    if (directComparison) _seed + simulationIndex
    else _seed + configIndex + simulationIndex*configs.size //looks random but unique and covers [seed,seed+numConfigs*numSimulations]
  }

  val configs: Seq[Configuration]
}