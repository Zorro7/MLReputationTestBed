package jaspr.sellerssim

import jaspr.acmelogistics.ACMEMultiConfiguration
import jaspr.acmelogistics.ACMEMultiConfiguration._
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
object SellerMultiConfiguration extends App {

  val parser = new scopt.OptionParser[SellerMultiConfiguration]("SellerConfiguration") {
    opt[Seq[String]]("strategy") required() action {(x,c) => c.copy(strategies = x)}
    opt[Int]("numRounds") required() action {(x,c) => c.copy(numRounds = x)}
    opt[Int]("numSimulations") required() action {(x,c) => c.copy(numSimulations = x)}
    opt[Double]("clientInvolvementLikelihood") required() action {(x,c) => c.copy(clientInvolvementLikelihood = x)}
    opt[Int]("memoryLimit") required() action {(x,c) => c.copy(memoryLimit = x)}
    opt[Int]("numClients") required() action {(x,c) => c.copy(numClients = x)}
    opt[Int]("numProviders") required() action {(x,c) => c.copy(numProviders = x)}
    opt[Double]("eventLikelihood") required() action {(x,c) => c.copy(eventLikelihood = x)}
    opt[Double]("eventEffects") required() action {(x,c) => c.copy(eventEffects = x)}
    opt[Double]("honestWitnessLikelihood") required() action {(x,c) => c.copy(honestWitnessLikelihood = x)}
    opt[Double]("pessimisticWitnessLikelihood") required() action {(x,c) => c.copy(pessimisticWitnessLikelihood = x)}
    opt[Double]("negationWitnessLikelihood") required() action {(x,c) => c.copy(negationWitnessLikelihood = x)}
    opt[Double]("randomWitnessLikelihood") required() action {(x,c) => c.copy(randomWitnessLikelihood = x)}
    opt[Double]("promotionWitnessLikelihood") required() action {(x,c) => c.copy(promotionWitnessLikelihood = x)}
    opt[Double]("slanderWitnessLikelihood") required() action {(x,c) => c.copy(slanderWitnessLikelihood = x)}
    opt[Double]("providersToPromote") required() action {(x,c) => c.copy(providersToPromote = x)}
    opt[Double]("providersToSlander") required() action {(x,c) => c.copy(providersToSlander = x)}
  }

  val argsplt =
    if (args.length == 0) {
      ("--strategy " +
                "jaspr.strategy.NoStrategy," +
                "jaspr.strategy.fire.Fire(0.5)," +
//                "jaspr.strategy.fire.Fire(0.0)," +
//                "jaspr.strategy.fire.MLFire(0.5)," +
//                "jaspr.strategy.fire.MLFire(0.0)," +
//                "jaspr.strategy.betareputation.BetaReputation," +
//                "jaspr.strategy.betareputation.Travos," +
////                "jaspr.strategy.blade.Blade(2)," +
//                "jaspr.strategy.habit.Habit(2)," +
//                "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.trees.J48;10;0.5;false)," +
//                "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.trees.J48;10;0.0;false)," +
//                "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.bayes.NaiveBayes;10;0.5;true)," +
//                "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.bayes.NaiveBayes;10;0.0;true)," +
        " --numRounds 1000 --numSimulations 10 --memoryLimit 1000 --clientInvolvementLikelihood 0.1 " +
        "--numClients 10 --numProviders 100 " +
        "--eventLikelihood 0 --eventEffects 0 " +
        "--honestWitnessLikelihood 0.9 --pessimisticWitnessLikelihood 0.2 --negationWitnessLikelihood 0.2 " +
        "--randomWitnessLikelihood 0.2 --promotionWitnessLikelihood 0.2 --slanderWitnessLikelihood 0.2 " +
        "--providersToPromote 0.2 --providersToSlander 0.2").split(" ")
    } else args

  println(argsplt.toList mkString("["," ","]"))

  parser.parse(argsplt, SellerMultiConfiguration()) match {
    case Some(x) =>
      Simulation(x)
    case None =>
  }
}


case class SellerMultiConfiguration(
                               strategies: Seq[String] = Nil,
                               numRounds: Int = 250,
                               numSimulations: Int = 1,
                               clientInvolvementLikelihood: Double = 0.01,
                               memoryLimit: Int = 100,
                               numClients: Int = 10,
                               numProviders: Int = 25,
                               eventLikelihood: Double = 0,
                               eventEffects: Double = 0,
                               honestWitnessLikelihood: Double = 0.1,
                               pessimisticWitnessLikelihood: Double = 0.1,
                               negationWitnessLikelihood: Double = 0.1,
                               randomWitnessLikelihood: Double = 0.1,
                               promotionWitnessLikelihood: Double = 0.1,
                               slanderWitnessLikelihood: Double = 0.1,
                               providersToPromote: Double = 0.1,
                               providersToSlander: Double = 0.1
                                ) extends MultiConfiguration {
  override val directComparison = false

  //  override val _seed = 1000

  override lazy val configs: Seq[Configuration] =
    strategies.map(x => {
      new SellerConfiguration(
        Strategy.forName(x), numRounds, numSimulations, clientInvolvementLikelihood, memoryLimit,
        numClients, numProviders, eventLikelihood,
        honestWitnessLikelihood, pessimisticWitnessLikelihood, negationWitnessLikelihood,
        randomWitnessLikelihood, promotionWitnessLikelihood, slanderWitnessLikelihood,
        providersToPromote, providersToSlander
      )
    })

}

class SellerConfiguration(override val strategy: Strategy,
                          override val numRounds: Int = 250,
                          override val numSimulations: Int = 1,
                          val clientInvolvementLikelihood: Double = 0.01,
                          val memoryLimit: Int = 100,
                          val numClients: Int = 50,
                          val numProviders: Int = 50,
                          val eventLikelihood: Double = 0,
                          val eventEffects: Double = 0,
                          val honestWitnessLikelihood: Double = 0.1,
                          val pessimisticWitnessLikelihood: Double = 0.1,
                          val negationWitnessLikelihood: Double = 0.1,
                          val randomWitnessLikelihood: Double = 0.1,
                          val promotionWitnessLikelihood: Double = 0.1,
                          val slanderWitnessLikelihood: Double = 0.1,
                          val providersToPromote: Double = 0.1,
                          val providersToSlander: Double = 0.1
                           ) extends Configuration {
  override def newSimulation(): Simulation = {
    new SellerSimulation(this)
  }

  // Basic utility gained in each interaction (0 if absolute service properties are used and 2/3 if preferences are used (Like this so random strategy has E[U]=0).
  val baseUtility = if (preferences(null).isEmpty) 0d else 2d/3d


  // Services that exist in the simulation
  var simcapabilities = for (i <- 1 to 10) yield new ProductPayload(i.toString)
  // Services that a given provider is capable of providing - and with associated performance properties.
  def capabilities(provider: Provider): Seq[ProductPayload] = {
    var caps = Chooser.sample(simcapabilities, 2)
    caps = caps.map(_.copy(
      quality = provider.properties.map(x =>
        x._1 -> addNoise(x._2.doubleValue)
//        x._1 -> x._2.doubleValue
      )
    ))
    caps
  }

  def addNoise(x: Double): Double = {
    Chooser.bound(x * Chooser.randomDouble(0.5,2), -1, 1)
//    Chooser.bound(x * Chooser.randomDouble(-2,2), -1, 1)
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
        agent.properties.mapValues(x => Property(x.name, addNoise(x.doubleValue)))
//    new Property("agentid", agent.id) :: Nil
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
//    TreeMap()
    new Property("Quality", Chooser.randomDouble(-1d,1d)) ::
      new Property("Timeliness", Chooser.randomDouble(-1d,1d)) ::
      Nil
  }


  def witnessModel(witness: Witness, network: Network): WitnessModel = {
    Chooser.choose(
      new HonestWitnessModel ::
        new PessimisticWitnessModel ::
        new NegationWitnessModel ::
        new RandomWitnessModel ::
        new PromotionWitnessModel(Chooser.sample(network.providers, (providersToPromote*numProviders).toInt)) ::
        new SlanderWitnessModel(Chooser.sample(network.providers, (providersToSlander*numProviders).toInt)) ::
        Nil,
      honestWitnessLikelihood ::
        pessimisticWitnessLikelihood ::
        negationWitnessLikelihood ::
        randomWitnessLikelihood ::
        promotionWitnessLikelihood ::
        slanderWitnessLikelihood ::
        Nil
    )
//    Chooser.ifHappens[WitnessModel](honestLikelihood)(
//      new HonestWitnessModel
//    )(
//      Chooser.choose(
//        Chooser.ifHappens(pessimisticLikelihood)(new PessimisticWitnessModel :: Nil)(Nil) ++ Nil
//          ::
//          new OptimisticWitnessModel ::
//          new NegationWitnessModel ::
//          new RandomWitnessModel ::
//          new PromotionWitnessModel(Chooser.sample(network.providers, numProviders/10)) ::
//          new SlanderWitnessModel(Chooser.sample(network.providers, numProviders/10)) ::
//          Nil
//      )
//    )

  }

}
