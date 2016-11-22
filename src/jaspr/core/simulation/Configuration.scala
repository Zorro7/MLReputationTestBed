package jaspr.core.simulation

import jaspr.core.agent.Client
import jaspr.core.strategy.Strategy

import scala.util.Random

/**
  * Created by phil on 15/03/16.
  */
trait Configuration {
  def newSimulation(): Simulation

  val numRounds: Int
  val numAgents: Int

  def strategy(agent: Client): Strategy
}

trait MultiConfiguration {

  val numSimulations: Int

  val directComparison: Boolean = true
  private val _simSeeds: List[Int] = List.fill(numSimulations)(Random.nextInt(Int.MaxValue))
  private val _configSeeds: List[Int] = List.fill(numSimulations)(Random.nextInt(Int.MaxValue))

  def seed(configIndex: Int, simulationIndex: Int) = {
    if (directComparison) _simSeeds(simulationIndex)
    else _simSeeds(simulationIndex) + _configSeeds(configIndex)
  }

  val resultStart: Int = 0
  val resultEnd: Int = -1
  val configs: Seq[Configuration]
}