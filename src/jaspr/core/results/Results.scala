package jaspr.core.results

import java.io.PrintWriter
import java.text.DecimalFormat

import jaspr.core.{Simulation, Configuration}

import scala.collection.mutable

/**
 * Created by phil on 26/01/16.
 */
class Results {

  private[this] val lock = new Object()

  val results = new mutable.LinkedHashMap[Configuration, List[Seq[Result]]]()
  val df = new DecimalFormat("000.00")

  def record(config: Configuration, simResult: Seq[Result]) = lock.synchronized {
    results.put(config, simResult :: results.getOrElse(config, Nil))
  }

  def printAll(funch: Result => Double) = lock.synchronized {
    val x = (for ((config,results) <- results) yield {
      results.map(_.map(funch).reverse.mkString(",")).mkString(config.toString+": ", "\n"+config.toString+": ", "")
    }).mkString("", "\n", "")
    println(x)
  }

  def printAverage(funch: Result => Double) = lock.synchronized {
    val x = results.keys.mkString("", "\t", "\n") +
      (for ((config,res) <- results) yield {
        average(res, funch).map(df.format)
      }).transpose.map(_.mkString("\t\t")).mkString("\n")
    println(x)
  }

  def saveAll(filename: String, funch: Result => Double) = lock.synchronized {
    val x = (for ((config,results) <- results) yield {
      results.map(_.map(funch).reverse.mkString(",")).mkString(config.toString+": ", "\n"+config.toString+": ", "")
    }).mkString("", "\n", "")
    new PrintWriter(filename) { write(x); close() }
  }

  def saveConfig(filename: String, config: Configuration, funch: Result => Double) = lock.synchronized {
    val x = results.getOrElse(config, List()).map(_.map(funch).reverse.mkString(",")).mkString(config.toString+": ", "\n"+config.toString+": ", "")
    new PrintWriter(filename) { write(x); close() }
  }

  def average(results: Seq[Seq[Result]], funch: Result => Double): Seq[Double] = {
    val tmp = results.map(_.map(funch))
    tmp.transpose.map(_.sum / tmp.size.toDouble).reverse
  }
}

class Result(simulation: Simulation) {

  val round = simulation.round
  val totalUtility: Double = simulation.network.utility()
}
