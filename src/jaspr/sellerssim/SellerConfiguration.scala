package jaspr.sellerssim

import jaspr.core.agent._
import jaspr.core.service.{Payload, ClientContext}
import jaspr.core.{MultiConfiguration, Network, Simulation, Configuration}
import jaspr.core.strategy.Strategy
import jaspr.sellerssim.agent._
import jaspr.sellerssim.service.ProductPayload
import jaspr.sellerssim.strategy.{Mlrs, MlrsDirect}
import jaspr.strategy.NoStrategy
import jaspr.strategy.betareputation.{BetaReputation, Travos}
import jaspr.strategy.blade.Blade
import jaspr.strategy.fire.{MLFire, Fire}
import jaspr.strategy.habit.Habit
import jaspr.utilities.Chooser
import weka.classifiers.Classifier
import weka.classifiers.`lazy`.{KStar, IBk}
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.functions.{MultilayerPerceptron, SMOreg, SMO}
import weka.classifiers.rules.OneR
import weka.classifiers.trees.{J48, RandomForest}

import scala.collection.immutable.{TreeMap, SortedMap}

/**
 * Created by phil on 21/03/16.
 */

class SellerMultiConfiguration extends MultiConfiguration {
  override val directComparison = true

//  override val _seed = 1100

  override lazy val configs: Seq[Configuration] =
    new SellerConfiguration(new NoStrategy) ::
      new SellerConfiguration(new Fire) ::
      new SellerConfiguration(new Fire(0)) ::
//      new SellerConfiguration(new MLFire) ::
//      new SellerConfiguration(new MLFire(0)) ::
//      new SellerConfiguration(new BetaReputation)::
//      new SellerConfiguration(new Travos) ::
//      new SellerConfiguration(new Blade(2)) ::
//      new SellerConfiguration(new Blade(10)) ::
//      new SellerConfiguration(new Habit(2)) ::
//        new SellerConfiguration(new Habit(10)) ::
//        new SellerConfiguration(new Mlrs(new NaiveBayes, 5)) ::
  Nil
}

class SellerConfiguration(override val strategy: Strategy) extends Configuration {
  override def newSimulation(): Simulation = {
    new SellerSimulation(this)
  }

  override val numSimulations: Int = 10
  override val numRounds: Int = 500
  val baseUtility = 0//2d/3d

  val clientIncolvementLikelihood = 0.1
  val numClients: Int = 10
  val numProviders: Int = 50

  // Records older than this number of rounds are removed from provenance stores
  val memoryLimit: Int = 500

  // Liklihood that a given service is affected by a freak event.
  val freakEventLikelihood = 0.0
  // Change to service attributes if it is affected by a freak event ((x + simulation.config.freakEventEffects) / 2d).
  def freakEventEffects = 0

  // Services that exist in the simulation
  var simcapabilities = for (i <- 1 to 10) yield new ProductPayload(i.toString)
  // Services that a given provider is capable of providing - and with associated performance properties.
  def capabilities(provider: Provider): Seq[ProductPayload] = {
    var caps = Chooser.sample(simcapabilities, 2)
    caps = caps.map(_.copy(
      quality = provider.properties.map(x =>
//        x._1 -> Chooser.bound(x._2.doubleValue + Chooser.randomDouble(-1,1), -1, 1)
        x._1 -> x._2.doubleValue
      )
    ))
    caps
  }

  // Properties of a provider agent
  def properties(agent: Agent): SortedMap[String,Property] = {
    //    TreeMap()
    new Property("Quality", Chooser.randomDouble(-1d,1d)) ::
      new Property("Timeliness", Chooser.randomDouble(-1d,1d)) ::
      Nil
  }

  def adverts(agent: Agent with Properties): SortedMap[String,Property] = {
    //        agent.properties.mapValues(x => Property(x.name, x.doubleValue + Chooser.randomDouble(-1.5,1.5))) //todo make this more something.
    //    agent.properties.mapValues(x => Property(x.name, x.doubleValue * Chooser.randomDouble(0.5, 2)))
    new Property("agentid", agent.id) :: Nil
  }


  // Context generation with required ppayload
  def clientContext(network: Network, client: Client, round: Int) = {
    val cap = Chooser.choose(simcapabilities).copy(
      quality = client.preferences.map(x =>
        x._1 -> x._2.doubleValue
      )
    )
    new ClientContext(client, round, cap, network.markets.head)
  }

  // Agent preferences - the qualities of a Payload that they want to have.
  // Rayings and Utility are computed relative to this (default to 0d if the property does not exist).
  def preferences(agent: Buyer): SortedMap[String,Property] = {
    TreeMap()
//    new Property("Quality", Chooser.randomDouble(-1d,1d)) ::
//      new Property("Timeliness", Chooser.randomDouble(-1d,1d)) ::
//      Nil
  }

  def witnessModel(witness: Witness, network: Network): WitnessModel = {
    Chooser.ifHappens[WitnessModel](0)(
      new HonestWitnessModel
    )(
      Chooser.choose(
//        new PessimisticWitnessModel ::
//          new OptimisticWitnessModel ::
//          new NegationWitnessModel ::
//          new RandomWitnessModel ::
          new PromotionWitnessModel(Chooser.sample(network.providers, numProviders/5)) ::
          new SlanderWitnessModel(Chooser.sample(network.providers, numProviders/5)) ::
          Nil
      )
    )

  }

}
