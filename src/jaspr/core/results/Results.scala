package jaspr.core.results

import java.io.PrintWriter
import java.text.DecimalFormat

import jaspr.core.simulation.{Configuration, Simulation}

import scala.collection.mutable

/**
  * Created by phil on 26/01/16.
  */
class Results {

  private[this] val lock = new Object()

  val results = new mutable.LinkedHashMap[Configuration, List[Seq[Result]]]()
  val df = new DecimalFormat("000.000")

  def record(config: Configuration, simResult: Seq[Result]) = lock.synchronized {
    results.put(config, simResult :: results.getOrElse(config, Nil))
  }

  def printIndividual(config: Configuration, simResults: List[Result], funch: Result => Double) = lock.synchronized {
    val x = simResults.map(funch).reverse.mkString(",")
    println("INTERIM:"+config.toString+": "+x)
  }

  def printAll(funch: Result => Double) = lock.synchronized {
    val x = (for ((config, results) <- results) yield {
      results.map(_.map(funch).reverse.mkString(",")).mkString(config.toString + ": ", "\n" + config.toString + ": ", "")
    }).mkString("", "\n", "")
    println(x)
  }

  def printAverage(funch: Result => Double) = lock.synchronized {
    val x = results.keys.mkString("", "\t", "\n") +
      (for ((config, res) <- results) yield {
        average(res, funch).map(df.format)
      }).transpose.map(_.mkString("\t")).mkString("\n")
    println(x)
  }

  def saveAll(filename: String, funch: Result => Double) = lock.synchronized {
    val x = (for ((config, results) <- results) yield {
      results.map(_.map(funch).reverse.mkString(",")).mkString(config.toString + ": ", "\n" + config.toString + ": ", "")
    }).mkString("", "\n", "")
    new PrintWriter(filename) {
      write(x);
      close()
    }
  }

  def saveConfig(filename: String, config: Configuration, funch: Result => Double) = lock.synchronized {
    val x = results.getOrElse(config, List()).map(_.map(funch).reverse.mkString(",")).mkString(config.toString + ": ", "\n" + config.toString + ": ", "")
    new PrintWriter(filename) {
      write(x);
      close()
    }
  }

  def average(results: Seq[Seq[Result]], funch: Result => Double): Seq[Double] = {
    val tmp = results.map(_.map(funch))
    tmp.transpose.map(_.sum / tmp.size.toDouble).reverse
  }

  def printChange(start: Int, end: Int, funch: Result => Double) = {
    val x =
      for ((config, res) <- results) yield {
        res.map(r =>
          funch(r(if (end >= 0) r.size - 1 - end else -end))
            - funch(r(if (start >= 0) r.size - 1 - start else -start))
        ).sum / res.size
      }
    println(x.map(df.format).mkString("", "\t", ""))
  }
}

class Result(simulation: Simulation) {

  val round = simulation.round
  val totalUtility: Double = simulation.network.utility()
  val recordsStored: Double = simulation.network.clients.map(x => x.getProvenance(x).size).sum / simulation.network.clients.size.toDouble
}
