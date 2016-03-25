package jaspr.sellerssim

import jaspr.core.agent._
import jaspr.core.service.{Payload, ClientContext}
import jaspr.core.{MultiConfiguration, Network, Simulation, Configuration}
import jaspr.core.strategy.Strategy
import jaspr.sellerssim.service.ProductPayload
import jaspr.sellerssim.strategy.{Mlrs, MlrsDirect}
import jaspr.strategy.NoStrategy
import jaspr.strategy.blade.Blade
import jaspr.strategy.fire.Fire
import jaspr.strategy.habit.Habit
import jaspr.utilities.Chooser

/**
 * Created by phil on 21/03/16.
 */

class SellerMultiConfiguration extends MultiConfiguration {
  override val directComparison = true

  override val _seed = 218118262

  override lazy val configs: Seq[Configuration] =
  new SellerConfiguration(new Habit()) ::
  new SellerConfiguration(new Blade()) ::
      new SellerConfiguration(new NoStrategy) ::
        new SellerConfiguration(new Fire) ::
        new SellerConfiguration(new Mlrs) ::
  //    new SellerConfiguration(new Travos) ::
  //    new SellerConfiguration(new BetaReputation)::
      Nil
}

class SellerConfiguration(override val strategy: Strategy) extends Configuration {
  override def newSimulation(): Simulation = {
    new SellerSimulation(this)
  }

  override val numSimulations: Int = 3
  override val numRounds: Int = 100

  val clientIncolvementLikelihood = 0.1
  val numClients: Int = 10
  val numProviders: Int = 50

  val memoryLimit: Int = 250

  val freakEventLikelihood = 0.1
  val freakEventEffects = -1d


  def capabilities(provider: Provider): Seq[ProductPayload] = {
    Chooser.sample(simcapabilities, Chooser.randomInt(1, 5))
  }

  def properties(agent: Agent): Map[String,Property] = {
    new Property("Quality", Chooser.randomDouble(-1,1)) ::
    new Property("Timeliness", Chooser.randomDouble(-1,1)) ::
    Nil
  }

  def adverts(agent: Agent with Properties): Map[String,Property] = {
//        agent.properties.mapValues(x => Property(x.name, x.doubleValue + Chooser.randomDouble(-1.5,1.5))) //todo make this more something.
//    agent.properties.mapValues(x => Property(x.name, x.doubleValue * Chooser.randomDouble(0.5, 2)))
        new Property("agentid", agent.id) :: Nil
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
