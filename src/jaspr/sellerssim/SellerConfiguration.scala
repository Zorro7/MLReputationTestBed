package jaspr.sellerssim

import jaspr.core.agent._
import jaspr.core.service.{Payload, ClientContext}
import jaspr.core.{MultiConfiguration, Network, Simulation, Configuration}
import jaspr.core.strategy.Strategy
import jaspr.sellerssim.service.ProductPayload
import jaspr.strategy.NoStrategy
import jaspr.strategy.fire.Fire
import jaspr.strategy.ipaw.{IpawEvents, Ipaw}
import jaspr.utilities.Chooser
import weka.classifiers.functions.LinearRegression

/**
 * Created by phil on 21/03/16.
 */

class SellerMultiConfiguration extends MultiConfiguration {
  override val directComparison = true

//  override val _seed = 1000

  override lazy val configs: Seq[Configuration] =
      new SellerConfiguration(new NoStrategy) ::
        new SellerConfiguration(new Fire) ::
  //    new SellerConfiguration(new Travos) ::
  //    new SellerConfiguration(new BetaReputation)::
      Nil
}

class SellerConfiguration(override val strategy: Strategy) extends Configuration {
  override def newSimulation(): Simulation = {
    new SellerSimulation(this)
  }

  override val numSimulations: Int = 10
  override val numRounds: Int = 500

  val clientIncolvementLikelihood = 0.1
  val numClients: Int = 10
  val numProviders: Int = 50

  val memoryLimit: Int = 100


  def capabilities(provider: Provider): Seq[ProductPayload] = {
    Chooser.sample(simcapabilities, Chooser.randomInt(1, 5))
  }

  def properties(agent: Agent): Map[String,Property] = {
    new Property("Quality", Chooser.randomDouble(-1,1)) ::
    new Property("Timeliness", Chooser.randomDouble(-1,1)) ::
    Nil
  }

  def adverts(agent: Agent with Properties): Map[String,Property] = {
    //    rawProperties.mapValues(x => x + Chooser.randomDouble(-1.5,1.5)) //todo make this more something.
    agent.properties.mapValues(x => Property(x.name, x.doubleValue * Chooser.randomDouble(0.5, 2)))
    //    agent.properties
    //    Map()
    //    new Property("agentid", agent.id) :: Nil
  }

  val simcapabilities = new ProductPayload("giblet") :: Nil
  def clientContext(network: Network, client: Client, round: Int) = {
    new ClientContext(
      client, round,
      new ProductPayload("giblet"),
      network.markets.head
    )
  }
}
