package jaspr.bootstrapsim

import jaspr.bootstrapsim.agent.Trustee
import jaspr.core.agent._
import jaspr.core.simulation.{MultiConfiguration, Simulation, Configuration}
import jaspr.core.strategy.Strategy
import jaspr.utilities.Chooser

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


  def adverts(agent: Trustee): SortedMap[String, Property] = {
    val ads: SortedMap[String,Property] = agent.properties.head._2 match {
      case GaussianProperty(_,0.9,_) => FixedProperty("1", true) :: FixedProperty("6", true) :: Nil
      case GaussianProperty(_,0.6,_) => FixedProperty("2", true) :: FixedProperty("4", true) :: Nil
      case GaussianProperty(_,0.4,_) => FixedProperty("3", true) :: FixedProperty("4", true) :: Nil
      case GaussianProperty(_,0.3,_) => FixedProperty("2", true) :: FixedProperty("3", true) :: FixedProperty("5", true) :: Nil
      case GaussianProperty(_,0.5,_) => FixedProperty("2", true) :: FixedProperty("4", true) :: FixedProperty("6", true) :: Nil
    }
    val fullAds: SortedMap[String,Property] = (1 to 6).map(x =>
      if (ads.contains(x.toString)) {
        ads(x.toString)
      } else {
        FixedProperty(x.toString, false)
      }
    ).toList
    println(fullAds)
    fullAds
  }

  def properties(agent: Agent): SortedMap[String,Property] = {
    Chooser.select(
      GaussianProperty("a", 0.9, 0.05) :: Nil,
      GaussianProperty("a", 0.6, 0.15) :: Nil,
      GaussianProperty("a", 0.4, 0.15) :: Nil,
      GaussianProperty("a", 0.3, 0.05) :: Nil, //0.3,0
      GaussianProperty("a", 0.5, 1) :: Nil //0.1 1
    )
  }

  def preferences(agent: Agent): SortedMap[String,Property] = {
    FixedProperty("a", 0.5) :: Nil
  }


  override def toString: String = _strategy.name
}
