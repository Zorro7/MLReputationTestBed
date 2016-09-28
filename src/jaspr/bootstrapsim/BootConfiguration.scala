package jaspr.bootstrapsim

import jaspr.core.agent.{Properties, FixedProperty, Agent, Client}
import jaspr.core.simulation.{MultiConfiguration, Simulation, Configuration}
import jaspr.core.strategy.Strategy

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 27/09/2016.
  */
object BootMultiConfiguration extends App {

  val parser = new scopt.OptionParser[BootMultiConfiguration]("SellerConfiguration") {
    opt[Seq[String]]("strategy") required() action { (x, c) => c.copy(strategies = x) }
  }

  val argsplt =
    if (args.length == 0) {
      ("--strategy " +
          "jaspr.strategy.NoStrategy," +
        "").split(" ")
    } else args

  println(argsplt.toList mkString("[", " ", "]"))

  parser.parse(argsplt, BootMultiConfiguration()) match {
    case Some(x) =>
      val results = Simulation(x)
      results.printChange(0, -1, _.recordsStored)
    case None =>
  }
}

case class BootMultiConfiguration(strategies: Seq[String] = Nil
                                          ) extends MultiConfiguration {
  override val directComparison = true

  override val resultStart: Int = 0
  override val resultEnd: Int = -1
//  override val _seed = 1

  override lazy val configs: Seq[Configuration] =
    strategies.map(x => {
      new BootConfiguration(
        _strategy = Strategy.forName(x)
      )
    })
}


class BootConfiguration(val _strategy: Strategy) extends Configuration {
  override def newSimulation(): Simulation = {
    new BootSimulation(this)
  }

  override def strategy(agent: Client): Strategy = _strategy

  override val numSimulations: Int = 1
  val numClients = 10
  val numProviders = 10
  val memoryLimit: Int = 100

  val trusteeLeaveLikelihood = 0.0
  val trusterLeaveLikelihood = 0.0

  override val numAgents: Int = numClients + numProviders
  override val numRounds: Int = 10


  def adverts(agent: Agent with Properties): SortedMap[String, FixedProperty] = {
    agent.properties
  }

  def properties(agent: Agent): SortedMap[String, FixedProperty] = {
    Nil
  }


  override def toString: String = _strategy.name
}
