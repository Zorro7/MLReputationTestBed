package jaspr.sellerssim.dynamicsimulation

import jaspr.core.agent._
import jaspr.core.service.ClientContext
import jaspr.core.simulation._
import jaspr.core.strategy.Strategy
import jaspr.sellerssim.agent.{HonestWitnessModel, Witness, WitnessModel}
import jaspr.sellerssim.service.ProductPayload
import jaspr.sellerssim.{SellerConfiguration, SellerNetwork, SellerSimulation}
import jaspr.strategy.NoStrategy
import jaspr.strategy.fire.Fire
import jaspr.utilities.Chooser

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 15/03/16.
  */


object DynamicSellerMultiConfiguration extends App {
  val multiconfig = new DynamicSellerMultiConfiguration()
  val results = Simulation(multiconfig)
}

class DynamicSellerMultiConfiguration extends MultiConfiguration {
  override val directComparison = true

  override lazy val configs: Seq[Configuration] =
    new DynamicSellerConfiguration(new Fire) ::
      new DynamicSellerConfiguration(new NoStrategy) ::
      Nil
}


class DynamicSellerConfiguration(val _strategy: Strategy) extends SellerConfiguration {

  override def newSimulation(): Simulation = {
    new SellerSimulation(this)
  }

  override def network(simulation: SellerSimulation): SellerNetwork = {
    new DynamicSellerNetwork(simulation)
  }

  override def strategy(agent: Client): Strategy = _strategy

  override val numSimulations: Int = 5
  override val numRounds: Int = 500

  override def numClients: Int = 25
  override def numProviders: Int = 25
  def clientAttrition: Double = 0.1
  def providerAttrition: Double = 0.1

  override def memoryLimit: Int = 100

  override def clientInvolvementLikelihood: Double = 0.1

  override def witnessRequestLikelihood: Double = 0.1

  override def baseUtility: Double = 0.5

  override def eventLikelihood: Double = 0d

  override def eventEffects: Double = 0d

  override def clientContext(network: Network with NetworkMarket, client: Client with Preferences, round: Int): ClientContext = {
    val cap = Chooser.choose(simcapabilities).copy(
      quality = client.preferences.map(x =>
        x._1 -> x._2.doubleValue
      )
    )
    new ClientContext(client, round, cap, network.market)
  }

  override var simcapabilities: Seq[ProductPayload] = new ProductPayload("a") :: Nil

  override def capabilities(provider: Provider): Seq[ProductPayload] = {
    simcapabilities.map(_.copy(
      quality = provider.properties.map(x =>
        x._1 -> x._2.doubleValue
      )
    ))
  }

  override def properties(agent: Agent): SortedMap[String, Property] = {
    Property("a", Chooser.randomDouble(-1, 1)) :: Nil
  }

  override def preferences(agent: Client): SortedMap[String, Property] = {
    Property("a", 0) :: Nil
  }

  override def adverts(agent: Agent with Properties): SortedMap[String, Property] = {
    Property("a", 1) :: Nil
  }

  override def witnessModel(witness: Witness, network: Network): WitnessModel = {
    new HonestWitnessModel
  }
}
