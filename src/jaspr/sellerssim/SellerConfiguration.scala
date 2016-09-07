package jaspr.sellerssim

import jaspr.core.agent._
import jaspr.core.service.ClientContext
import jaspr.core.simulation.{Configuration, MultiConfiguration, Network, Simulation}
import jaspr.core.strategy.Strategy
import jaspr.sellerssim.agent._
import jaspr.sellerssim.service.ProductPayload
import jaspr.utilities.Chooser

import scala.collection.immutable.SortedMap

/**
 * Created by phil on 21/03/16.
 */
object StaticSellerMultiConfiguration extends App {

  val parser = new scopt.OptionParser[StaticSellerMultiConfiguration]("SellerConfiguration") {
    opt[Seq[String]]("strategy") required() action {(x,c) => c.copy(strategies = x)}
    opt[Int]("numRounds") required() action {(x,c) => c.copy(numRounds = x)}
    opt[Int]("numSimulations") required() action {(x,c) => c.copy(numSimulations = x)}
    opt[Double]("clientInvolvementLikelihood") required() action {(x,c) => c.copy(clientInvolvementLikelihood = x)}
    opt[Int]("memoryLimit") required() action {(x,c) => c.copy(memoryLimit = x)}
    opt[Int]("numClients") required() action {(x,c) => c.copy(numClients = x)}
    opt[Int]("numProviders") required() action {(x,c) => c.copy(numProviders = x)}
    opt[Int]("numSimCapabilities") required() action {(x,c) => c.copy(numSimCapabilities = x)}
    opt[Int]("numProviderCapabilities") required() action {(x,c) => c.copy(numProviderCapabilities = x)}
    opt[Double]("noiseRange") required() action {(x,c) => c.copy(noiseRange = x)}
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
        "jaspr.strategy.NoStrategy," +
//        "jaspr.sellerssim.strategy.general.mlrs2.MlrsB(weka.classifiers.bayes.NaiveBayes;2;round;250.;2.0;true;false),"+
//        "jaspr.sellerssim.strategy.general.mlrs2.MlrsB(weka.classifiers.bayes.NaiveBayes;2;round;250.;2.0;true;true),"+
//        "jaspr.sellerssim.strategy.general.mlrs2.Mlrs(weka.classifiers.bayes.NaiveBayes;2;2.0;true;false),"+
//        "jaspr.sellerssim.strategy.general.mlrs2.Mlrs(weka.classifiers.bayes.NaiveBayes;2;2.0;true;true),"+
        "jaspr.strategy.fire.Fire(0.0)," +
        "jaspr.strategy.fire.Fire(0.5)," +
        "jaspr.strategy.betareputation.BetaReputation," +
        "jaspr.strategy.betareputation.Travos,"+
//          "jaspr.strategy.blade.Blade(2)," +
//        "jaspr.strategy.habit.Habit(2),"+
//        "jaspr.strategy.stereotype.Burnett,"+
        " --numSimulations 10 " +
        "--honestWitnessLikelihood 1 " +
        "--pessimisticWitnessLikelihood 0 " +
        "--optimisticWitnessLikelihood 0 " +
        "--randomWitnessLikelihood 0 " +
        "--negationWitnessLikelihood 0 " +
        "--promotionWitnessLikelihood 0 " +
        "--slanderWitnessLikelihood 0 " +
        "--providersToPromote 0.25 " +
        "--providersToSlander 0.25 " +
        "--numClients 10 --numProviders 50 " +
        "--eventLikelihood 0 " +
        "--clientInvolvementLikelihood 0.1 " +
        "--eventEffects 0 " +
        "--numRounds 1000 " +
        "--memoryLimit 100 " +
        "--numSimCapabilities 5 " +
        "--numProviderCapabilities 5 " +
        "--noiseRange 1. " +
        "--numTerms 1 " +
        "--witnessRequestLikelihood 0.2 " +
        "--numAdverts 1 " +
        "--usePreferences true").split(" ")
    } else args

  println(argsplt.toList mkString("["," ","]"))

  parser.parse(argsplt, StaticSellerMultiConfiguration()) match {
    case Some(x) =>
      val results = Simulation(x)
      results.printChange(0,-1, _.recordsStored)
    case None =>
  }
}


case class StaticSellerMultiConfiguration(
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
                               noiseRange: Double = 1d,
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
  override val directComparison = true

  override val resultStart: Int = -memoryLimit
  override val resultEnd: Int = -1
//  override val _seed = 1



  override lazy val configs: Seq[Configuration] =
    strategies.map(x => {
      new StaticSellerConfiguration(
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
        noiseRange = noiseRange,
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




abstract class SellerConfiguration extends Configuration {

  def clientInvolvementLikelihood: Double

  def numClients: Int

  def numProviders: Int

  override val numAgents = numClients + numProviders

  def witnessRequestLikelihood: Double

  var simcapabilities: Seq[ProductPayload]

  def baseUtility: Double

  def eventLikelihood: Double

  def eventEffects: Double

  def memoryLimit: Int

  def properties(agent: Agent): SortedMap[String, Property]

  def preferences(agent: Client): SortedMap[String, Property]

  def capabilities(agent: Provider): Seq[ProductPayload]

  def adverts(agent: Agent with Properties): SortedMap[String, Property]

  def clientContext(network: Network, agent: Client, round: Int): ClientContext

  def witnessModel(witness: Witness, network: Network): WitnessModel

  def network(simulation: SellerSimulation): SellerNetwork
}




class StaticSellerConfiguration(override val strategy: Strategy,
                                override val numRounds: Int,
                                override val numSimulations: Int,
                                override val clientInvolvementLikelihood: Double,
                                override val witnessRequestLikelihood: Double,
                                override val memoryLimit: Int,
                                override val numClients: Int,
                                override val numProviders: Int,
                                val numSimCapabilities: Int,
                                val numProviderCapabilities: Int,
                                val noiseRange: Double,
                                val numTerms: Int,
                                val numAdverts: Int,
                                val usePreferences: Boolean,
                                override val eventLikelihood: Double,
                                override val eventEffects: Double,
                                val honestWitnessLikelihood: Double,
                                val pessimisticWitnessLikelihood: Double,
                                val optimisticWitnessLikelihood: Double,
                                val negationWitnessLikelihood: Double,
                                val randomWitnessLikelihood: Double,
                                val promotionWitnessLikelihood: Double,
                                val slanderWitnessLikelihood: Double,
                                val providersToPromote: Double,
                                val providersToSlander: Double
                           ) extends SellerConfiguration {
  override def newSimulation(): Simulation = {
    new SellerSimulation(this)
  }
  override def network(simulation: SellerSimulation): SellerNetwork = {
    new StaticSellerNetwork(simulation)
  }


  override val baseUtility: Double = 1d/2d

//  val baseUtility = if (usePreferences) 2d/3d else 1d/2d
//  val baseUtility = 1d

  def addNoise(x: Double): Double = {
    //    Chooser.bound(x + Chooser.randomDouble(-noiseRange/2d, noiseRange/2d), -1, 1)
    val ret = (x + Chooser.randomDouble(-1*noiseRange,1*noiseRange))/2d
//    val ret = (x + Chooser.nextGaussian()*noiseRange)/2d
//    val ret = (x+noiseRange*Chooser.randomDouble(-1,1))/(noiseRange+1)
    //    Chooser.bound(x + Chooser.randomDouble(-1,1), -1, 1)
//    println(x, ret)
//   val ret =  x + Chooser.randomDouble(-1d,1d)
    ret
  }

  // Services that exist in the simulation
  override var simcapabilities: Seq[ProductPayload] = for (i <- 1 to numSimCapabilities) yield new ProductPayload(i.toString)
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

  // Context generation with required ppayload
  def clientContext(network: Network, client: Client, round: Int) = {
    val cap = Chooser.choose(simcapabilities).copy(
      quality = client.preferences.map(x =>
        x._1 -> x._2.doubleValue
      )
    )
    new ClientContext(client, round, cap, network.markets.head)
  }



  // Properties of a provider agent
  def properties(agent: Agent): SortedMap[String,Property] = {
    (1 to numTerms).map(x => new Property(x.toString, Chooser.randomDouble(-1d,1d))).toList
  }

  // Agent preferences - the qualities of a Payload that they want to have.
  // Rayings and Utility are computed relative to this (default to 0d if the property does not exist).
  def preferences(agent: Client): SortedMap[String,Property] = {
    if (usePreferences) (1 to numTerms).map(x => new Property(x.toString, Chooser.randomDouble(-1d,1d))).toList
    else Nil//(1 to numTerms).map(x => new Property(x.toString, 0d)).toList
  }

  def adverts(agent: Agent with Properties): SortedMap[String,Property] = {
    agent.properties.take(numAdverts).mapValues(x => Property(x.name, addNoise(x.doubleValue)))
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
