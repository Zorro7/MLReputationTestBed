package jaspr.sellerssim

import jaspr.core.agent._
import jaspr.core.service.{Payload, ClientContext}
import jaspr.core.{MultiConfiguration, Network, Simulation, Configuration}
import jaspr.core.strategy.Strategy
import jaspr.sellerssim.agent.Buyer
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

/**
 * Created by phil on 21/03/16.
 */

class SellerMultiConfiguration extends MultiConfiguration {
  override val directComparison = true

//  override val _seed = 1

  override lazy val configs: Seq[Configuration] =
    new SellerConfiguration(new NoStrategy) ::
      new SellerConfiguration(new Fire) ::
      new SellerConfiguration(new MLFire) ::
//      new SellerConfiguration(new BetaReputation)::
//      new SellerConfiguration(new Travos) ::
//      new SellerConfiguration(new Blade()) ::
//      new SellerConfiguration(new Habit(2)) ::
//        new SellerConfiguration(new Habit(5)) ::
//        new SellerConfiguration(new Mlrs(new NaiveBayes, 2)) ::
//        new SellerConfiguration(new Mlrs(new NaiveBayes, 5)) ::
//        new SellerConfiguration(new Mlrs(new OneR, 2)) ::
//        new SellerConfiguration(new Mlrs(new OneR, 5)) ::
//        new SellerConfiguration(new Mlrs(new J48, 2)) ::
//        new SellerConfiguration(new Mlrs(new J48, 5)) ::
//        new SellerConfiguration(new Mlrs(new RandomForest, 0)) ::
//        new SellerConfiguration(new Mlrs(new RandomForest, 5)) ::
//        new SellerConfiguration(new Mlrs(new RandomForest, 10)) ::
//        new SellerConfiguration(new Mlrs(new SMO, 2)) ::
//        new SellerConfiguration(new Mlrs(new MultilayerPerceptron, 0)) ::
//        new SellerConfiguration(new Mlrs(new SMO, 5)) ::
//          new SellerConfiguration(new Mlrs(new IBk, 0)) ::
//          new SellerConfiguration(new Mlrs(new IBk, 5)) ::
//          new SellerConfiguration(new Mlrs(new IBk, 10)) ::
//        new SellerConfiguration(new Mlrs(new KStar, 0)) ::
//        new SellerConfiguration(new Mlrs(new KStar, 5)) ::
//        new SellerConfiguration(new Mlrs(new KStar, 10)) ::
  Nil
}

class SellerConfiguration(override val strategy: Strategy) extends Configuration {
  override def newSimulation(): Simulation = {
    new SellerSimulation(this)
  }

  override val numSimulations: Int = 10
  override val numRounds: Int = 5000

  val clientIncolvementLikelihood = 0.1
  val numClients: Int = 10
  val numProviders: Int = 500

  val memoryLimit: Int = 100

  val freakEventLikelihood = 0.0
  def freakEventEffects = -1d

  var simcapabilities = for (i <- 1 to 25) yield new ProductPayload(i.toString)
  def capabilities(provider: Provider): Seq[ProductPayload] = {
    var caps = Chooser.sample(simcapabilities, 5)
    caps = caps.map(_.copy(
      quality = provider.properties.map(x =>
        x._1 -> Chooser.bound(x._2.doubleValue + Chooser.randomDouble(-1,1), -1, 1)
      )
    ))
    caps
  }

  def clientContext(network: Network, client: Client, round: Int) = {
    new ClientContext(
      client, round,
      Chooser.choose(simcapabilities),
      network.markets.head
    )
  }

  def properties(agent: Agent): Map[String,Property] = {
//    Map()
    new Property("Quality", Chooser.randomDouble(-1,1)) ::
    new Property("Timeliness", Chooser.randomDouble(-1,1)) ::
    Nil
  }

  def adverts(agent: Agent with Properties): Map[String,Property] = {
//        agent.properties.mapValues(x => Property(x.name, x.doubleValue + Chooser.randomDouble(-1.5,1.5))) //todo make this more something.
//    agent.properties.mapValues(x => Property(x.name, x.doubleValue * Chooser.randomDouble(0.5, 2)))
        new Property("agentid", agent.id) :: Nil
  }


  def changeRatings(agent: Buyer): Map[String,Double] => Map[String,Double] = {
    def honest(ratings: Map[String,Double]) = ratings
    def invert(ratings: Map[String,Double]) = ratings.mapValues(-_)
    def random(ratings: Map[String,Double]) = ratings.mapValues(x => Chooser.randomDouble(-1,1))
    def positive(ratings: Map[String,Double]) = ratings.mapValues(x => (x + 1) / 2) // normalizes the rating to between 0 and 1
    def negative(ratings: Map[String,Double]) = ratings.mapValues(x => (x - 1) / 2) // normalizes the rating to between 0 and -1
    choose(honest(_), invert(_), random(_), positive(_), negative(_))
//    honest
  }


  def choose[V](stuff: V*): V = Chooser.choose(stuff)
}
