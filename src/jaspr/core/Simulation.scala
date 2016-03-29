package jaspr.core

import jaspr.core.results.{Result, Results}
import jaspr.utilities.Chooser

/**
 * Created by phil on 15/03/16.
 */


object Simulation {

  def apply(multiConfig: MultiConfiguration) {
    val results: Results = new Results

    for ((config, configIndex) <- multiConfig.configs.zipWithIndex) {
      for (simulationIndex <- 0 until config.numSimulations) {
        val simulationSeed = multiConfig.seed(configIndex, simulationIndex)
        Chooser.setSeed(simulationSeed)
        jaspr.debug(1000, "\n\n----- CONFIG " + configIndex + " (" + config + "), SIMULATION " + simulationIndex + ", seed: " + simulationSeed + " -----")
        val simulation = config.newSimulation()
        results.record(config, simulation.run())
      }
      results.saveConfig(config.toString + ".res", config, _.totalUtility)
    }

    println("\n---\n")
    results.printAll(_.totalUtility)
    results.printAverage(_.totalUtility)
    println(results.results.keys.mkString("\t"))
  }
}


abstract class Simulation {

  val config: Configuration
  val network: Network

  private var currentRound = 0
  def round = currentRound

  private var results: List[Result] = Nil

  def run(): List[Result] = {
    while (round <= config.numRounds) {
      currentRound += 1
      jaspr.debug(100, "\n------ ROUND "+round+", Utility: "+network.utility()+" ------")
      results = act() :: results
    }
    results
  }

  def act(): Result
}
