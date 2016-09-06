package jaspr.dynamicsim

import jaspr.core.agent._
import jaspr.core.service.{ClientContext, Payload}
import jaspr.core.strategy.Strategy
import jaspr.core.{Configuration, MultiConfiguration, Network, Simulation}
import jaspr.sellerssim.{SellerConfiguration, SellerSimulation}
import jaspr.sellerssim.agent.{HonestWitnessModel, Witness, WitnessModel}
import jaspr.sellerssim.service.ProductPayload
import jaspr.strategy.NoStrategy
import jaspr.strategy.fire.Fire
import jaspr.utilities.Chooser

import scala.collection.immutable.SortedMap

/**
 * Created by phil on 15/03/16.
 */





class DynamicConfiguration(override val strategy: Strategy) extends SellerConfiguration {

  override def newSimulation(): Simulation = {
    new SellerSimulation(this)
  }

  override val numSimulations: Int = 1
  override val numRounds: Int = 100

  override def numClients: Int = 5
  override def numProviders: Int = 5
  override def memoryLimit: Int = 100

  override def clientInvolvementLikelihood: Double = 0.1
  override def witnessRequestLikelihood: Double = 0.1

  override def baseUtility: Double = 1
  override def eventLikelihood: Double = 0d
  override def eventEffects: Double = 0d

  override def clientContext(network: Network, client: Client, round: Int): ClientContext = {
    val cap = Chooser.choose(simcapabilities).copy(
      quality = client.preferences.map(x =>
        x._1 -> x._2.doubleValue
      )
    )
    new ClientContext(client, round, cap, network.markets.head)
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
    Property("a", 1) :: Nil
  }
  override def preferences(agent: Client): SortedMap[String, Property] = {
    Property("a", 1) :: Nil
  }
  override def adverts(agent: Agent with Properties): SortedMap[String, Property] = {
    Property("a", 1) :: Nil
  }

  override def witnessModel(witness: Witness, network: Network): WitnessModel = {
    new HonestWitnessModel
  }
}


class DynamicMultiConfiguration extends MultiConfiguration {
  override val directComparison = true

  override lazy val configs: Seq[Configuration] =
    new DynamicConfiguration(new Fire) ::
    new DynamicConfiguration(new NoStrategy) ::
      Nil
}