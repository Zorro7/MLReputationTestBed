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
    opt[Int]("numSimCapabilities") required() action {(x,c) => c.copy(numSimCapabilities = x)}
    opt[Int]("numProviderCapabilities") required() action {(x,c) => c.copy(numProviderCapabilities = x)}
    opt[Int]("numTerms") required() action {(x,c) => c.copy(numTerms = x)}
    opt[Int]("numAdverts") required() action {(x,c) => c.copy(numAdverts = x)}
    opt[Boolean]("usePreferences") required() action {(x,c) => c.copy(usePreferences = x)}
    opt[Double]("eventLikelihood") required() action {(x,c) => c.copy(eventLikelihood = x)}
    opt[Double]("eventEffects") required() action {(x,c) => c.copy(eventEffects = x)}
    opt[Double]("honestWitnessLikelihood") required() action {(x,c) => c.copy(honestWitnessLikelihood = x)}
    opt[Double]("pessimisticWitnessLikelihood") required() action {(x,c) => c.copy(pessimisticWitnessLikelihood = x)}
    opt[Double]("optimisticWitnessLikelihood") required() action {(x,c) => c.copy(optimisticWitnessLikelihood = x)}
    opt[Double]("negationWitnessLikelihood") required() action {(x,c) => c.copy(negationWitnessLikelihood = x)}
    opt[Double]("randomWitnessLikelihood") required() action {(x,c) => c.copy(randomWitnessLikelihood = x)}
    opt[Double]("promotionWitnessLikelihood") required() action {(x,c) => c.copy(promotionWitnessLikelihood = x)}
    opt[Double]("slanderWitnessLikelihood") required() action {(x,c) => c.copy(slanderWitnessLikelihood = x)}
    opt[Double]("providersToPromote") required() action {(x,c) => c.copy(providersToPromote = x)}
    opt[Double]("providersToSlander") required() action {(x,c) => c.copy(providersToSlander = x)}
    opt[Double]("witnessRequestLikelihood") required() action {(x,c) => c.copy(witnessRequestLikelihood = x)}
  }

  val argsplt =
    if (args.length == 0) {
      ("--strategy " +
//        "jaspr.strategy.NoStrategy," +
//        "jaspr.sellerssim.strategy.general.BasicML(weka.classifiers.bayes.NaiveBayes;2)," +
//        "jaspr.sellerssim.strategy.general.FireLike(weka.classifiers.bayes.NaiveBayes;2)," +
//        "jaspr.sellerssim.strategy.general.BasicStereotype(weka.classifiers.bayes.NaiveBayes;2)," +
//        "jaspr.sellerssim.strategy.general.FireLikeStereotype(weka.classifiers.bayes.NaiveBayes;2)," +
//        "jaspr.sellerssim.strategy.general.TravosLikeSlim," +
//        "jaspr.sellerssim.strategy.general.TravosLike(weka.classifiers.bayes.NaiveBayes;2)," +
//        "jaspr.sellerssim.strategy.general.BasicContext(weka.classifiers.bayes.NaiveBayes;2)," +
//        "jaspr.sellerssim.strategy.general.FireLikeContext(weka.classifiers.bayes.NaiveBayes;2)," +
//        "jaspr.strategy.fire.Fire(0.5)," +
//        "jaspr.strategy.fire.Fire(0.0)," +
//        "jaspr.strategy.fire.MLFire(0.5), " +
//        "jaspr.strategy.fire.MLFire(0.0)," +
//        "jaspr.strategy.betareputation.BetaReputation," +
//        "jaspr.strategy.betareputation.MLTravos_provider," +
//        "jaspr.strategy.betareputation.Travos,"+
          "jaspr.strategy.blade.Blade(2)," +
//          "jaspr.strategy.blade.Blade(5)," +
        //
//        "jaspr.strategy.habit.Habit(2),"+
//        "jaspr.strategy.habit.Habit(5),"+
//        "jaspr.strategy.stereotype.Burnett," +
//        "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.trees.J48;10;0.5;false)," +
//        "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.trees.J48;10;0.0;false)," +
//        "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.functions.SMO;5;0.5;true)," +
//        "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.trees.RandomForest;5;0.5;true)," +
//        "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.lazy.KStar;5;0.5;true)," +
//        "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.bayes.NaiveBayes;2;0.5;true),"+
    //        "jaspr.sellerssim.strategy.Mlrs(weka.classifiers.bayes.NaiveBayes;10;0.0;true)," +
        " --numSimulations 100 " +
        "--honestWitnessLikelihood 1 " +
        "--pessimisticWitnessLikelihood 0 " +
        "--optimisticWitnessLikelihood 0 " +
        "--randomWitnessLikelihood 0 " +
        "--negationWitnessLikelihood 0 " +
        "--promotionWitnessLikelihood 0 " +
        "--slanderWitnessLikelihood 0 " +
        "--providersToPromote 0.25 " +
        "--providersToSlander 0.25 " +
        "--numClients 100 --numProviders 100 " +
        "--eventLikelihood 0 " +
        "--clientInvolvementLikelihood 0.1 " +
        "--eventEffects 0 " +
        "--numRounds 1000 " +
        "--memoryLimit 100 " +
        "--numSimCapabilities 5 " +
        "--numProviderCapabilities 5 " +
        "--numTerms 5 " +
        "--witnessRequestLikelihood 0.1 " +
        "--numAdverts 2 " +
        "--usePreferences true").split(" ")
    } else args

  println(argsplt.toList mkString("["," ","]"))

  parser.parse(argsplt, SellerMultiConfiguration()) match {
    case Some(x) =>
      val results = Simulation(x)
      results.printChange(0,-1, _.recordsStored)
    case None =>
  }
}


case class SellerMultiConfiguration(
                               strategies: Seq[String] = Nil,
                               numRounds: Int = 250,
                               numSimulations: Int = 1,
                               clientInvolvementLikelihood: Double = 0.01,
                               witnessRequestLikelihood: Double = 0.1,
                               memoryLimit: Int = 100,
                               numClients: Int = 10,
                               numProviders: Int = 25,
                               numSimCapabilities: Int = 10,
                               numProviderCapabilities: Int = 5,
                               numTerms: Int = 2,
                               numAdverts: Int = 2,
                               usePreferences: Boolean = true,
                               eventLikelihood: Double = 0,
                               eventEffects: Double = 0,
                               honestWitnessLikelihood: Double = 0.1,
                               pessimisticWitnessLikelihood: Double = 0.1,
                               optimisticWitnessLikelihood: Double = 0.1,
                               negationWitnessLikelihood: Double = 0.1,
                               randomWitnessLikelihood: Double = 0.1,
                               promotionWitnessLikelihood: Double = 0.1,
                               slanderWitnessLikelihood: Double = 0.1,
                               providersToPromote: Double = 0.1,
                               providersToSlander: Double = 0.1
                                ) extends MultiConfiguration {
  override val directComparison = false

  override val resultStart: Int = -memoryLimit
  override val resultEnd: Int = -1
//  override val _seed = 1



  override lazy val configs: Seq[Configuration] =
    strategies.map(x => {
      new SellerConfiguration(
        strategy = Strategy.forName(x),
        numRounds = numRounds,
        numSimulations = numSimulations,
        clientInvolvementLikelihood = clientInvolvementLikelihood,
        witnessRequestLikelihood = witnessRequestLikelihood,
        memoryLimit = memoryLimit,
        numClients = numClients,
        numProviders = numProviders,
        numSimCapabilities = numSimCapabilities,
        numProviderCapabilities = numProviderCapabilities,
        numTerms = numTerms,
        numAdverts = numAdverts,
        usePreferences = usePreferences,
        eventLikelihood = eventLikelihood,
        eventEffects = eventEffects,
        honestWitnessLikelihood = honestWitnessLikelihood,
        pessimisticWitnessLikelihood = pessimisticWitnessLikelihood,
        optimisticWitnessLikelihood = optimisticWitnessLikelihood,
        negationWitnessLikelihood = negationWitnessLikelihood,
        randomWitnessLikelihood = randomWitnessLikelihood,
        promotionWitnessLikelihood = promotionWitnessLikelihood,
        slanderWitnessLikelihood = slanderWitnessLikelihood,
        providersToPromote = providersToPromote,
        providersToSlander = providersToSlander
      )
    })
}

class SellerConfiguration(override val strategy: Strategy,
                          override val numRounds: Int,
                          override val numSimulations: Int,
                          val clientInvolvementLikelihood: Double,
                          val witnessRequestLikelihood: Double,
                          val memoryLimit: Int,
                          val numClients: Int,
                          val numProviders: Int,
                          val numSimCapabilities: Int,
                          val numProviderCapabilities: Int,
                          val numTerms: Int,
                          val numAdverts: Int,
                          val usePreferences: Boolean,
                          val eventLikelihood: Double,
                          val eventEffects: Double,
                          val honestWitnessLikelihood: Double,
                          val pessimisticWitnessLikelihood: Double,
                          val optimisticWitnessLikelihood: Double,
                          val negationWitnessLikelihood: Double,
                          val randomWitnessLikelihood: Double,
                          val promotionWitnessLikelihood: Double,
                          val slanderWitnessLikelihood: Double,
                          val providersToPromote: Double,
                          val providersToSlander: Double
                           ) extends Configuration {
  override def newSimulation(): Simulation = {
    new SellerSimulation(this)
  }


  // Basic utility gained in each interaction (0 if absolute service properties are used and 2/3 if preferences are used (Like this so random strategy has E[U]=0).
  val baseUtility = if (usePreferences) 2d/3d else 1d/2d
//  val baseUtility = 2d/3d

  // Services that exist in the simulation
  var simcapabilities = for (i <- 1 to numSimCapabilities) yield new ProductPayload(i.toString)
  // Services that a given provider is capable of providing - and with associated performance properties.
  def capabilities(provider: Provider): Seq[ProductPayload] = {
    var caps = Chooser.sample(simcapabilities, numProviderCapabilities)
    caps = caps.map(_.copy(
      quality = provider.properties.map(x =>
        x._1 -> addNoise(x._2.doubleValue)
      )
    ))
//    println(provider, caps)
    caps
  }

  def addNoise(x: Double): Double = {
    Chooser.bound(x + Chooser.randomDouble(-0.5,0.5), -1, 1)
//    x + Chooser.randomDouble(-0.5,0.5)
  }

  // Properties of a provider agent
  def properties(agent: Agent): SortedMap[String,Property] = {
    (1 to numTerms).map(x => new Property(x.toString, Chooser.randomDouble(-1d,1d))).toList
  }

  def adverts(agent: Agent with Properties): SortedMap[String,Property] = {
    agent.properties.take(numAdverts).mapValues(x => Property(x.name, addNoise(x.doubleValue)))
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
    if (usePreferences) (1 to numTerms).map(x => new Property(x.toString, Chooser.randomDouble(-1d,1d))).toList
    else (1 to numTerms).map(x => new Property(x.toString, 0d)).toList
  }


  def witnessModel(witness: Witness, network: Network): WitnessModel = {
    Chooser.choose(
      new HonestWitnessModel ::
        new PessimisticWitnessModel ::
        new OptimisticWitnessModel ::
        new NegationWitnessModel ::
        new RandomWitnessModel ::
        new PromotionWitnessModel(Chooser.sample(network.providers, (providersToPromote*numProviders).toInt)) ::
        new SlanderWitnessModel(Chooser.sample(network.providers, (providersToSlander*numProviders).toInt)) ::
        Nil,
      honestWitnessLikelihood ::
        pessimisticWitnessLikelihood ::
        optimisticWitnessLikelihood ::
        negationWitnessLikelihood ::
        randomWitnessLikelihood ::
        promotionWitnessLikelihood ::
        slanderWitnessLikelihood ::
        Nil
    )
  }

}
