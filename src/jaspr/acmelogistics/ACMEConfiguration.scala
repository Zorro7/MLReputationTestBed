package jaspr.acmelogistics

import jaspr.acmelogistics.ACMEConfiguration
import jaspr.acmelogistics.agent.{Mine, Refinery, Shipper}
import jaspr.acmelogistics.service.GoodPayload
import jaspr.core.agent.{Client, Properties, Agent, Property}
import jaspr.core.service.{ClientContext}
import jaspr.core.{Network, MultiConfiguration, Simulation, Configuration}
import jaspr.core.strategy.Strategy
import jaspr.strategy.NoStrategy
import jaspr.strategy.betareputation.{BetaReputation, Travos}
import jaspr.strategy.fire.Fire
import jaspr.acmelogistics.strategy.ipaw.{IpawEvents, RecordFire, Ipaw}
import jaspr.utilities.{Chooser}
import weka.classifiers.`lazy`.KStar
import weka.classifiers.functions.LinearRegression
import weka.classifiers.rules.OneR
import weka.classifiers.trees.J48

/**
 * Created by phil on 17/03/16.
 */

class ACMEMultiConfiguration extends MultiConfiguration {
  override val directComparison = true

//  override val _seed = 1000

  override lazy val configs: Seq[Configuration] =
//    new ACMEConfiguration(new NoStrategy) ::
//    new ACMEConfiguration(new RecordFire) ::
//      new ACMEConfiguration(new Fire) ::
//    new ACMEConfiguration(new Travos) ::
//    new ACMEConfiguration(new BetaReputation)::
//    new ACMEConfiguration(new Ipaw(new J48, true)) ::
//      new ACMEConfiguration(new Ipaw(new KStar, false)) ::
    new ACMEConfiguration(new Ipaw(new LinearRegression, false)) ::
    new ACMEConfiguration(new IpawEvents(new LinearRegression, false)) ::
//    new ACMEConfiguration(new Ipaw(new OneR, true)) ::
      Nil
}

class ACMEConfiguration(override val strategy: Strategy) extends Configuration {

  override def newSimulation(): Simulation = new ACMESimulation(this)

  override val numSimulations: Int = 5
  override val numRounds: Int = 500

  val memoryLimit = 100

  val numProviders = 25
  val numClients = 1
  val numShippers = numProviders
  val numRefineries = numProviders
  val numMines = numProviders
  val numCompositions = numProviders

  val defaultServiceDuration = 5

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
    //    rawProperties.mapValues(x => x + Chooser.randomDouble(-1.5,1.5)) //todo make this more something.
    agent.properties.mapValues(x => Property(x.name, x.doubleValue * Chooser.randomDouble(0.5, 2)))
//    new Property("agentid", agent.id) :: Nil
  }
}

