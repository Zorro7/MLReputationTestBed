package jaspr.sellerssim.dynamicsimulation

import java.util.Random

import jaspr.core.agent._
import jaspr.core.service.ClientContext
import jaspr.core.simulation._
import jaspr.core.strategy.Strategy
import jaspr.sellerssim.agent._
import jaspr.sellerssim.service.ProductPayload
import jaspr.sellerssim.strategy.general.BasicML
import jaspr.sellerssim.strategy.mlrs.Mlrs
import jaspr.sellerssim.{SellerConfiguration, SellerNetwork, SellerSimulation}
import jaspr.strategy.NoStrategy
import jaspr.strategy.blade.Blade
import jaspr.strategy.fire.Fire
import jaspr.strategy.habit.Habit
import jaspr.strategy.stereotype.Burnett
import jaspr.utilities.Chooser

import scala.collection.immutable.{SortedMap, TreeMap}

/**
  * Created by phil on 15/03/16.
  */


object DynamicSellerMultiConfiguration extends App {

  val parser = new scopt.OptionParser[DynamicSellerMultiConfiguration]("SellerConfiguration") {
    opt[Seq[String]]("strategy") required() action { (x, c) => c.copy(strategies = x) }
    opt[Int]("numRounds") required() action { (x, c) => c.copy(numRounds = x) }
    opt[Int]("numSimulations") required() action { (x, c) => c.copy(numSimulations = x) }
    opt[Double]("clientInvolvementLikelihood") required() action { (x, c) => c.copy(clientInvolvementLikelihood = x) }
    opt[Double]("providersAvailable") required() action { (x, c) => c.copy(providersAvailable = x) }
    opt[Int]("memoryLimit") required() action { (x, c) => c.copy(memoryLimit = x) }
    opt[Int]("numClients") required() action { (x, c) => c.copy(numClients = x) }
    opt[Int]("numProviders") required() action { (x, c) => c.copy(numProviders = x) }
    opt[Int]("numSimCapabilities") required() action { (x, c) => c.copy(numSimCapabilities = x) }
    opt[Int]("numProviderCapabilities") required() action { (x, c) => c.copy(numProviderCapabilities = x) }
    opt[Double]("sigma") required() action { (x, c) => c.copy(sigma = x) }
    opt[Int]("numTerms") required() action { (x, c) => c.copy(numTerms = x) }
    opt[Int]("numAdverts") required() action { (x, c) => c.copy(numAdverts = x) }
    opt[Int]("numPreferences") required() action { (x, c) => c.copy(numPreferences = x) }
    opt[Double]("eventLikelihood") required() action { (x, c) => c.copy(eventLikelihood = x) }
    opt[Double]("eventEffects") required() action { (x, c) => c.copy(eventEffects = x) }
    opt[Double]("honestWitnessLikelihood") required() action { (x, c) => c.copy(honestWitnessLikelihood = x) }
    opt[Double]("pessimisticWitnessLikelihood") required() action { (x, c) => c.copy(pessimisticWitnessLikelihood = x) }
    opt[Double]("optimisticWitnessLikelihood") required() action { (x, c) => c.copy(optimisticWitnessLikelihood = x) }
    opt[Double]("negationWitnessLikelihood") required() action { (x, c) => c.copy(negationWitnessLikelihood = x) }
    opt[Double]("randomWitnessLikelihood") required() action { (x, c) => c.copy(randomWitnessLikelihood = x) }
    opt[Double]("promotionWitnessLikelihood") required() action { (x, c) => c.copy(promotionWitnessLikelihood = x) }
    opt[Double]("slanderWitnessLikelihood") required() action { (x, c) => c.copy(slanderWitnessLikelihood = x) }
    opt[Double]("providersToPromote") required() action { (x, c) => c.copy(providersToPromote = x) }
    opt[Double]("providersToSlander") required() action { (x, c) => c.copy(providersToSlander = x) }
    opt[Double]("witnessesAvailable") required() action { (x, c) => c.copy(witnessesAvailable = x) }
    opt[Double]("clientAttrition") required() action { (x, c) => c.copy(clientAttrition = x) }
    opt[Double]("providerAttrition") required() action { (x, c) => c.copy(providerAttrition = x) }
  }

  val argsplt =
    if (args.length == 0) {
      ("--strategy " +
        "jaspr.strategy.NoStrategy," +
//        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.trees.RandomForest;2;0d;1d;weka.classifiers.functions.LinearRegression;2.0;false;false;false;false),"+
////        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.trees.RandomForest;2;0d;1d;weka.classifiers.functions.LinearRegression;2.0;false;false;true;false),"+
//        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.trees.M5P;0;-1d;-1d;weka.classifiers.functions.LinearRegression;2.0;false;false;false;false),"+
////        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.trees.M5P;0;-1d;-1d;weka.classifiers.functions.LinearRegression;2.0;false;false;true;false),"+
//        "jaspr.sellerssim.strategy.general.BasicML(weka.classifiers.bayes.NaiveBayes;2;0d;1d)," +
//        "jaspr.sellerssim.strategy.general.BasicML(weka.classifiers.trees.RandomForest;0;0d;1d)," +
//        "jaspr.sellerssim.strategy.general.BasicML(weka.classifiers.trees.M5P;0;0d-1d)," +
//        "jaspr.sellerssim.strategy.general.BasicContext(weka.classifiers.bayes.NaiveBayes;2;0d;1d;false)," +
//        "jaspr.sellerssim.strategy.general.BasicContext(weka.classifiers.trees.RandomForest;2;0d;1d;false)," +
//        "jaspr.sellerssim.strategy.general.BasicContext(weka.classifiers.trees.M5P;0;0d;1d;false)," +
//        "jaspr.sellerssim.strategy.general.FireLikeContext(weka.classifiers.bayes.NaiveBayes;5;0d;1d;false)," +
        "jaspr.strategy.fire.Fire(0.5;false)," +
//        "jaspr.strategy.fire.Fire(0.0;false)," +
//        "jaspr.strategy.betareputation.BRS(0.5)," +
//        "jaspr.strategy.betareputation.BRS(0.0)," +
//        "jaspr.strategy.betareputation.Travos(0.5)," +
//        "jaspr.strategy.blade.Blade(2;0d;1d)," +
//        "jaspr.strategy.habit.Habit(2;0d;1d),"+
        " --numSimulations 3 " +
        "--eventLikelihood 0 " +
        "--honestWitnessLikelihood 1 " +
        "--pessimisticWitnessLikelihood 0 " +
        "--optimisticWitnessLikelihood 0 " +
        "--randomWitnessLikelihood 0 " +
        "--negationWitnessLikelihood 0 " +
        "--promotionWitnessLikelihood 0 " +
        "--slanderWitnessLikelihood 0 " +
        "--providersToPromote 0.25 " +
        "--providersToSlander 0.25 " +
        "--numClients 10 --numProviders 100 " +
        "--clientInvolvementLikelihood 1 --witnessesAvailable 5 --providersAvailable 20 " +
        "--eventEffects 0 " +
        "--numRounds 100 " +
        "--memoryLimit 500 " +
        "--numSimCapabilities 5 --numProviderCapabilities 5 " +
        "--sigma 0.05d " +
        "--numTerms 3 --numPreferences 0 " +
        "--numAdverts 5 " +
        "--providerAttrition 0.0 --clientAttrition 0.0").split(" ")
    } else args

  println(argsplt.toList mkString("[", " ", "]"))

  parser.parse(argsplt, DynamicSellerMultiConfiguration()) match {
    case Some(x) =>
      val results = Simulation(x)
      results.printChange(0, -1, _.recordsStored)
    case None =>
  }
}

case class DynamicSellerMultiConfiguration(
                                            strategies: Seq[String] = Nil,
                                            numRounds: Int = 250,
                                            override val numSimulations: Int = 1,
                                            clientInvolvementLikelihood: Double = 0.01,
                                            providersAvailable: Double = 0.1,
                                            witnessesAvailable: Double = 0.1,
                                            memoryLimit: Int = 100,
                                            numClients: Int = 10,
                                            numProviders: Int = 25,
                                            numSimCapabilities: Int = 10,
                                            numProviderCapabilities: Int = 5,
                                            sigma: Double = 1d,
                                            numTerms: Int = 2,
                                            numAdverts: Int = 2,
                                            numPreferences: Int = 2,
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
                                            providersToSlander: Double = 0.1,
                                            clientAttrition: Double = 0.0,
                                            providerAttrition: Double = 0.0
                                         ) extends MultiConfiguration {
  override val directComparison = true

  override val resultStart: Int = -Math.min(numRounds, memoryLimit)
  override val resultEnd: Int = -1
//      override val _seed = 27800974


  override lazy val configs: Seq[Configuration] =
    strategies.map(x => {
      new DynamicSellerConfiguration(
        _strategy = Strategy.forName(x),
        numRounds = numRounds,
        clientInvolvementLikelihood = clientInvolvementLikelihood,
        providersAvailable = providersAvailable,
        witnessesAvailable = witnessesAvailable,
        memoryLimit = memoryLimit,
        numClients = numClients,
        numProviders = numProviders,
        numSimCapabilities = numSimCapabilities,
        numProviderCapabilities = numProviderCapabilities,
        _sigma = sigma,
        numTerms = numTerms,
        numAdverts = numAdverts,
        numPreferences = numPreferences,
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
        providersToSlander = providersToSlander,
        clientAttrition = clientAttrition,
        providerAttrition = providerAttrition
      )
    })
}


class DynamicSellerConfiguration(val _strategy: Strategy,
                                 override val numRounds: Int,
                                 override val clientInvolvementLikelihood: Double,
                                 val providersAvailable: Double,
                                 override val witnessesAvailable: Double,
                                 override val memoryLimit: Int,
                                 override val numClients: Int,
                                 override val numProviders: Int,
                                 val numSimCapabilities: Int,
                                 val numProviderCapabilities: Int,
                                 _sigma: Double,
                                 val numTerms: Int,
                                 val numAdverts: Int,
                                 val numPreferences: Int,
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
                                 val providersToSlander: Double,
                                 val clientAttrition: Double,
                                 val providerAttrition: Double
                                ) extends SellerConfiguration {

  override def newSimulation(): Simulation = {
    _simcapabilities =
      (1 to numSimCapabilities).map(x =>
        new ProductPayload(x.toString, randomProperties(Chooser.randomDouble(simCapMinMean, simCapMaxMean), sigma, numTerms).toList)
//        new ProductPayload(x.toString, FixedProperty("1", r) :: Nil)
      )
    new DynamicSellerSimulation(this)
  }

  override def network(simulation: SellerSimulation): SellerNetwork = {
    new DynamicSellerNetwork(simulation.asInstanceOf[DynamicSellerSimulation])
  }

  override def strategy(agent: Client): Strategy = _strategy


  def witnessModel(witness: Witness, network: Network): WitnessModel = {
    Chooser.choose(
      new HonestWitnessModel ::
        new PessimisticWitnessModel ::
        new OptimisticWitnessModel ::
        new NegationWitnessModel ::
        new RandomWitnessModel ::
        new PromotionWitnessModel(Chooser.sample(network.providers, (providersToPromote * numProviders).toInt)) ::
        new SlanderWitnessModel(Chooser.sample(network.providers, (providersToSlander * numProviders).toInt)) ::
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

  override def capabilities(provider: Provider): Seq[ProductPayload] = {
    val caps = Chooser.sample(simcapabilities, numProviderCapabilities)
    caps.map(c => c.copy(
      quality = addNoise(provider.properties, c.properties)
    ))
  }

  def addNoise(competency: SortedMap[String,Property], productProperty: SortedMap[String,Property]): SortedMap[String,Property] = {
    competency.map(c => c._1 -> {
      productProperty.get(c._1) match {
        case Some(x) =>
//          GaussianProperty(c._1, (c._2.doubleValue + noiseRange*Chooser.randomDouble(-1,1))/(noiseRange+1), 0.1)
          GaussianProperty(c._1, (c._2.doubleValue + x.doubleValue)/2d, sigma)
//          GaussianProperty(c._1, (c._2.doubleValue+2d*Chooser.randomDouble(0,1))/3d, sigma)
//          GaussianProperty(c._1, x.doubleValue, sigma)
      }
    })
  }

  override val baseUtility: Double = 1

//  val minMean: Double = 0
//  val maxMean: Double = 2
  val provCapMinMean: Double = 0
  val provCapMaxMean: Double = 1
  val simCapMinMean: Double = 0.5
  val simCapMaxMean: Double = 0.5
  val prefMinMean: Double = 0
  val prefMaxMean: Double = 1

  val sigma = _sigma
  val fixedPreference = 1

  // Services that exist in the simulation
  private var _simcapabilities: Seq[ProductPayload] = Nil //set above in newSimulation(..)
  override def simcapabilities: Seq[ProductPayload] = _simcapabilities

  override def clientContext(network: Network with NetworkMarket, client: Client with Preferences, round: Int) = {
    val cap = Chooser.choose(simcapabilities).copy(
      quality = client.preferences.map(x => x._1 -> x._2.sample)
    )
    new ClientContext(client, round, cap, network.market)
  }

  override def properties(agent: Agent): SortedMap[String, Property] = {
    randomProperties(Chooser.randomDouble(provCapMinMean, provCapMaxMean), sigma, numTerms).toList
  }

  override def preferences(agent: Client): SortedMap[String, Property] = {
    if (numPreferences == 0) {
      (1 to numTerms).map(x => FixedProperty(x.toString, fixedPreference)).toList
    } else {
      Chooser.sample(randomProperties(Chooser.randomDouble(prefMinMean, prefMaxMean), sigma, numTerms), numPreferences).toList
    }
  }

  def randomProperties(mean: Double, sigma: Double, num: Int): Seq[Property] = {
    (1 to num).map(
      x => GaussianProperty(x.toString, Chooser.nextGaussian()*sigma + mean, sigma)
//      x => FixedProperty(x.toString, Chooser.nextGaussian()*sigma + mean)
    )
  }



  def adverts(agent: Agent with Properties): SortedMap[String,Property] = {
    agent.properties.take(numAdverts).values.map(x =>x.sample).toList
  }

  def adverts(payload: ProductPayload, agent: Agent with Properties): SortedMap[String,Property] = {
    payload.quality.take(numAdverts).values.map(x => x.sample).toList
  }


  override def toString: String = _strategy.name
}
