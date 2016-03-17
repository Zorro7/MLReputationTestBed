package jaspr.acmelogistics

import jaspr.acmelogistics.agent.{Mine, Refinery, Shipper}
import jaspr.core.agent.{Client, Properties, Agent, Property}
import jaspr.core.service.{ClientContext}
import jaspr.core.{Network, MultiConfiguration, Simulation, Configuration}
import jaspr.core.strategy.Strategy
import jaspr.strategy.{Fire, NoStrategy}
import jaspr.utilities.{Chooser}

/**
 * Created by phil on 17/03/16.
 */
class ACMEConfiguration(override val strategy: Strategy) extends Configuration {

  override def newSimulation(): Simulation = new ACMESimulation(this)

  override val numSimulations: Int = 1
  override val numRounds: Int = 10

  val memoryLimit = 100

  val numClients = 1
  val numShippers = 0
  val numRefineries = 0
  val numMines = 3


  def clientContext(network: Network, client: Client, round: Int): ClientContext = {
    new ClientContext(client, round, network.markets.head)
  }

  def properties(agent: Agent): Map[String,Property] = {
    agent match {
      case _: Shipper =>
        Property("Timeliness", Chooser.randomDouble(-3,1)) ::
        //        Property("Capacity") -> Chooser.randomDouble(-1,1),
        Property("Competence", Chooser.randomDouble(-1, 1)) :: Nil
      case _: Refinery =>
        Property("Rate", Chooser.randomDouble(-3,1)) ::
        Property("MetalPurity", Chooser.randomDouble(-1,1)) ::
        Property("OrePurityReq", Chooser.randomDouble(-1,1)) :: Nil
      case _: Mine =>
        Property("Rate", Chooser.randomDouble(-3,1)) ::
        Property("OreWetness",  Chooser.randomDouble(-1,1)) ::
        Property("OrePurity", Chooser.randomDouble(-1,1)) :: Nil
      case _ => Map[String,Property]()
    }
  }

  def adverts(agent: Agent with Properties): Map[String,Property] = {
    //    rawProperties.mapValues(x => x + Chooser.randomDouble(-1.5,1.5)) //todo make this more something.
    agent.properties.mapValues(x => Property(x.name, x.doubleValue * Chooser.randomDouble(0.5, 2)))
  }
}

class ACMEMultiConfiguration extends MultiConfiguration {
  override val directComparison = true

  override lazy val configs: Seq[Configuration] =
//    new ACMEConfiguration(new Fire) ::
      new ACMEConfiguration(new NoStrategy) ::
      Nil
}