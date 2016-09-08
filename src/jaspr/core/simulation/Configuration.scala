package jaspr.core.simulation

import jaspr.core.agent.Client
import jaspr.core.strategy.Strategy

import scala.util.Random

/**
  * Created by phil on 15/03/16.
  */
trait Configuration {
  def newSimulation(): Simulation

  val numSimulations: Int
  val numRounds: Int
  val numAgents: Int

  def strategy(agent: Client): Strategy
}

trait MultiConfiguration {

  val directComparison: Boolean = true
  val _seed: Int = Random.nextInt(Int.MaxValue)

  def seed(configIndex: Int, simulationIndex: Int) = {
    if (directComparison) _seed + simulationIndex
    else _seed + configIndex + simulationIndex * configs.size //looks random but unique and covers [seed,seed+numConfigs*numSimulations]
  }

  val resultStart: Int = 0
  val resultEnd: Int = -1
  val configs: Seq[Configuration]
}