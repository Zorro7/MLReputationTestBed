package jaspr.acmelogistics

import jaspr.acmelogistics.agent.{ACMEEvent, Mine, Refinery, Shipper}
import jaspr.acmelogistics.service.GoodPayload
import jaspr.core.agent._
import jaspr.core.service.ClientContext
import jaspr.core.{Network, MultiConfiguration, Simulation, Configuration}
import jaspr.core.strategy.Strategy
import jaspr.strategy.NoStrategy
import jaspr.strategy.betareputation.{BetaReputation, Travos}
import jaspr.strategy.fire.Fire
import jaspr.acmelogistics.strategy.ipaw.{IpawEvents, RecordFire, Ipaw}
import jaspr.utilities.Chooser
import weka.classifiers.`lazy`.KStar
import weka.classifiers.functions.LinearRegression
import weka.classifiers.rules.OneR
import weka.classifiers.trees.J48

/**
 * Created by phil on 17/03/16.
 */


object Configuration extends App {

  val parser = new scopt.OptionParser[ACMEMultiConfiguration]("") {
    opt[Seq[String]]("strategy") action{
      (x,c) => c.copy(strategies = x)
    }
    opt[Int]("numRounds") required() action {
      (x,c) => c.copy(numRounds = x)
    }
    opt[Int]("numSimulations") required() action {
      (x,c) => c.copy(numSimulations = x)
    }
    opt[Int]("memoryLimit") required() action {
      (x,c) => c.copy(memoryLimit = x)
    }
    opt[Int]("numProviders") required() action {
      (x,c) => c.copy(numProviders = x)
    }
    opt[Int]("defaultServiceDuration") required() action {
      (x,c) => c.copy(defaultServiceDuration = x)
    }
    opt[Double]("eventProportion") required() action {
      (x,c) => c.copy(eventProportion = x)
    }
    opt[Double]("eventLikelihood") required() action {
      (x,c) => c.copy(eventLikelihood = x)
    }
    opt[Int]("eventDelay") required() action {
      (x,c) => c.copy(eventDelay = x)
    }
    opt[Boolean]("adverts") required() action{
      (x,c) => c.copy(adverts = x)
    }
  }

  val argsplt =
    ("--strategy jaspr.strategy.NoStrategy,jaspr.acmelogistics.strategy.ipaw.RecordFire --numRounds 3 --numSimulations 2 " +
      "--memoryLimit 100 --numProviders 25 --defaultServiceDuration 10 " +
      "--eventProportion 1 --eventLikelihood 2 --eventDelay 2 " +
      "--adverts false").split(" ")
  parser.parse(argsplt, ACMEMultiConfiguration()) match {
    case Some(x) =>
      Simulation(x)
    case None =>
  }

}

case class ACMEMultiConfiguration(strategies: Seq[String] = Nil,
                                  numRounds: Int = 250,
                                  numSimulations: Int = 1,
                                  memoryLimit: Int = 100,
                                  numProviders: Int = 25,
                                  defaultServiceDuration: Int = 5,
                                  eventProportion: Double = 0,
                                  eventLikelihood: Double = 0,
                                  eventDelay: Int = 0,
                                  adverts: Boolean = true
                                   ) extends MultiConfiguration {
  override val directComparison = false

//  override val _seed = 1000

  override lazy val configs: Seq[Configuration] =
    strategies.map(x => new ACMEConfiguration(
      Class.forName(x).newInstance().asInstanceOf[Strategy],
      numRounds, numSimulations, memoryLimit,
      numProviders, defaultServiceDuration, eventProportion,
      eventLikelihood, eventDelay, adverts
    ))
}

class ACMEConfiguration(override val strategy: Strategy,
                        val numRounds: Int = 250,
                        val numSimulations: Int = 1,
                        val memoryLimit: Int = 100,
                        val numProviders: Int = 25,
                        val defaultServiceDuration: Int = 5,
                        val eventProportion: Double = 0,
                        val eventLikelihood: Double = 0,
                        val eventDelay: Int = 0,
                        val adverts: Boolean = true
                          ) extends Configuration {

  override def toString: String = {
    List(
      strategy, numRounds, numSimulations,
      memoryLimit, numProviders, defaultServiceDuration,
      eventProportion, eventLikelihood, eventDelay, adverts
    ).toString
  }

  override def newSimulation(): Simulation = new ACMESimulation(this)

//  override val numSimulations: Int = 10
//  override val numRounds: Int = 500

//  val memoryLimit = 250

//  val numProviders = 50
  val numClients = 1
  val numShippers = numProviders
  val numRefineries = numProviders
  val numMines = numProviders
  val numCompositions = numProviders

//  val defaultServiceDuration = 5

//  val eventProportion = 0.1
//  val eventLikelihood = 0.1
//  val eventDelay: Int = 1
  def nextEvents(providers: Seq[Provider]) = {
    Chooser.ifHappens(eventLikelihood)(
      new ACMEEvent(
        Chooser.sample(providers, (providers.size*eventProportion).toInt),
        eventDelay
      ) :: Nil
    )(Nil)
  }

  def clientContext(network: Network, client: Client, round: Int): ClientContext = {
    new ClientContext(
      client, round,
      new GoodPayload(Chooser.randomDouble(0,1), Chooser.randomDouble(0,1)),
      network.markets.head
    )
  }

  def properties(agent: Agent): Map[String,Property] = {
    agent match {
      case _: Shipper =>
        Property("Timeliness", Chooser.randomDouble(-1,1)) ::
        Property("Competence", Chooser.randomDouble(0, 1)) ::
        Property("Capacity", Chooser.randomDouble(0,1)) :: Nil
      case _: Refinery =>
        Property("Rate", Chooser.randomDouble(-1,1)) ::
        Property("MetalPurity", Chooser.randomDouble(0,1)) ::
        Property("OrePurityReq", Chooser.randomDouble(0,1)) :: Nil
      case _: Mine =>
        Property("Rate", Chooser.randomDouble(-1,1)) ::
        Property("OreWetness",  Chooser.randomDouble(0,1)) ::
        Property("OrePurity", Chooser.randomDouble(0,1)) :: Nil
      case _ => Map[String,Property]()
    }
  }

  def adverts(agent: Agent with Properties): Map[String,Property] = {
    if (adverts) agent.properties.mapValues(x => Property(x.name, x.doubleValue * Chooser.randomDouble(0.5, 2)))
    else new Property("agentid", agent.id) :: Nil
    //    rawProperties.mapValues(x => x + Chooser.randomDouble(-1.5,1.5)) //todo make this more something.

  }
}

