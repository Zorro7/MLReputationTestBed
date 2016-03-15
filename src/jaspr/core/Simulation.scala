package jaspr.core

import jaspr.core.results.{Result, Results}
import jaspr.utilities.Chooser

/**
 * Created by phil on 15/03/16.
 */


object Simulation extends App {

  val multiConfig = new MultiConfiguration

  val results: Results = new Results

  for ((config,configIndex) <- multiConfig.configs.zipWithIndex) {
    for (simulationIndex <- 0 until config.numSimulations) {
      val simulationSeed = multiConfig.seed(configIndex, simulationIndex)
      Chooser.setSeed(simulationSeed)
      println("\n\n----- CONFIG "+configIndex+" ("+config+"), SIMULATION "+simulationIndex+", seed: "+simulationSeed+" -----")
      val simulation = config.newSimulation()
      results.record(config, simulation.run())
    }
    results.saveConfig(config.toString+".res", config, _.totalUtility)
  }

  println("\n---\n")
  results.printAll(_.totalUtility)
  results.printAverage(_.totalUtility)
  println(results.results.keys.mkString("\t"))
}


abstract class Simulation(val config: Configuration) {

  private var currentRound = 0
  def round = currentRound

  val network: Network

  def run(): Seq[Result]
}
