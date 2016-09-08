package jaspr.sellerssim.dynamicsimulation

import jaspr.core.agent._
import jaspr.core.service.ClientContext
import jaspr.core.simulation._
import jaspr.core.strategy.Strategy
import jaspr.sellerssim.agent.{HonestWitnessModel, Witness, WitnessModel}
import jaspr.sellerssim.service.ProductPayload
import jaspr.sellerssim.strategy.general.mlrs2.Mlrs
import jaspr.sellerssim.{SellerConfiguration, SellerNetwork, SellerSimulation}
import jaspr.strategy.NoStrategy
import jaspr.strategy.blade.Blade
import jaspr.strategy.fire.Fire
import jaspr.strategy.habit.Habit
import jaspr.strategy.stereotype.Burnett
import jaspr.utilities.Chooser
import weka.classifiers.bayes.NaiveBayes

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
  override val resultStart: Int = -100

  override lazy val configs: Seq[Configuration] =
//    new DynamicSellerConfiguration(new Mlrs(new NaiveBayes, 2, 2, true, false)) ::
//    new DynamicSellerConfiguration(new Blade(2)) ::
//    new DynamicSellerConfiguration(new Habit(2)) ::
//    new DynamicSellerConfiguration(new Burnett) ::
    new DynamicSellerConfiguration(new Fire) ::
    new DynamicSellerConfiguration(new NoStrategy) ::
      Nil
}


class DynamicSellerConfiguration(val _strategy: Strategy) extends SellerConfiguration {

  override def newSimulation(): Simulation = {
    new DynamicSellerSimulation(this)
  }

  override def network(simulation: SellerSimulation): SellerNetwork = {
    new DynamicSellerNetwork(simulation.asInstanceOf[DynamicSellerSimulation])
  }

  override def strategy(agent: Client): Strategy = _strategy

  override val numSimulations: Int = 5
  override val numRounds: Int = 1000

  override def numClients: Int = 10
  override def numProviders: Int = 25
  val clientAttrition: Double = 0.0
  val providerAttrition: Double = 0.0

  override def memoryLimit: Int = 100

  override def clientInvolvementLikelihood: Double = 0.1
  override def witnessRequestLikelihood: Double = 0.2

  override def baseUtility: Double = 0.5

  override def eventLikelihood: Double = 0d
  override def eventEffects: Double = 0d



  override def witnessModel(witness: Witness, network: Network): WitnessModel = {
    new HonestWitnessModel
  }

  val noiseRange = 1d
  def addNoise(x: Double): Double = {
    (x + Chooser.randomDouble(-1 * noiseRange, 1 * noiseRange)) / 2d
  }

  val numSimCapabilities = 10
  override var simcapabilities: Seq[ProductPayload] = for (i <- 1 to numSimCapabilities) yield new ProductPayload(i.toString)

  val numProviderCapabilities = 10
  override def capabilities(provider: Provider): Seq[ProductPayload] = {
    Chooser.sample(simcapabilities, numProviderCapabilities).map(_.copy(
      quality = provider.properties.map(x =>
        x._1 -> addNoise(x._2.doubleValue)
      )
    ))
  }

  override def clientContext(network: Network with NetworkMarket, client: Client with Preferences, round: Int) = {
    val cap = Chooser.choose(simcapabilities).copy(
      quality = client.preferences.map(x =>
        x._1 -> x._2.doubleValue
      )
    )
    new ClientContext(client, round, cap, network.market)
  }


  val numTerms = 3
  override def properties(agent: Agent): SortedMap[String, Property] = {
    (1 to numTerms).map(x => new Property(x.toString, Chooser.randomDouble(-1d, 1d))).toList
  }

  val numPreferences = 0
  override def preferences(agent: Client): SortedMap[String, Property] = {
    if (numPreferences == 0) {
      (1 to numTerms).map(x => new Property(x.toString, 0d)).toList
    } else {
      Chooser.sample(1 to numTerms, numPreferences).map(
        x => new Property(x.toString, Chooser.randomDouble(-1d, 1d))
      ).toList
    }
  }

  val numAdverts = 3
  override def adverts(agent: Agent with Properties): SortedMap[String, Property] = {
    agent.properties.take(numAdverts).mapValues(x => Property(x.name, addNoise(x.doubleValue)))
  }


  override def toString: String = _strategy.name
}
