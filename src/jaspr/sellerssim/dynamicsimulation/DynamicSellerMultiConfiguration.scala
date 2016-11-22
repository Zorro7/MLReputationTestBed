package jaspr.sellerssim.dynamicsimulation

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
    opt[Double]("noiseRange") required() action { (x, c) => c.copy(noiseRange = x) }
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
        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.trees.RandomForest;2;0d;1d;weka.classifiers.functions.LinearRegression;2.0;false;false;false;false),"+
////        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.trees.M5P;0;weka.classifiers.functions.LinearRegression;2.0;false;false;false;false),"+
////        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.trees.RandomForest;2;weka.classifiers.functions.LinearRegression;2.0;true;true;false;false),"+
////        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.trees.RandomForest;2;weka.classifiers.functions.LinearRegression;0.0;false;false;false;false),"+
////////        "jaspr.sellerssim.strategy.general.FireLike(weka.classifiers.bayes.NaiveBayes;2),"+
////////        "jaspr.sellerssim.strategy.general.BasicML(weka.classifiers.bayes.NaiveBayes;2),"+
////////        "jaspr.sellerssim.strategy.general.FireLikeContext(weka.classifiers.bayes.NaiveBayes;2;false),"+
////////        "jaspr.sellerssim.strategy.general.BasicContext(weka.classifiers.bayes.NaiveBayes;2;false),"+
////        "jaspr.sellerssim.strategy.general.FireLike(weka.classifiers.trees.RandomForest;2),"+
////        "jaspr.sellerssim.strategy.general.BasicML(weka.classifiers.trees.RandomForest;2),"+
////        "jaspr.sellerssim.strategy.general.FireLikeContext(weka.classifiers.trees.RandomForest;2;false),"+
////        "jaspr.sellerssim.strategy.general.BasicContext(weka.classifiers.trees.RandomForest;2;false),"+
//////                "jaspr.strategy.fire.Fire(0.0;true)," +
////        ////        "jaspr.strategy.fire.FireContext(0.0;false)," +
////        ////        "jaspr.strategy.fire.FireContext(0.0;true)," +
        "jaspr.strategy.fire.Fire(0.5;false)," +
        "jaspr.strategy.fire.Fire(0.0;false)," +
////////        //        "jaspr.strategy.fire.Fire(0.5;true)," +
        "jaspr.strategy.betareputation.BetaReputation(0.5;0.5)," +
        "jaspr.strategy.betareputation.BetaReputation(0.0;0.5)," +
////////        //                "jaspr.strategy.betareputation.BetaReputation(1d)," +
        "jaspr.strategy.habit.Habit(2;0d;1d),"+
        "jaspr.strategy.betareputation.Travos(0.5)," +
        "jaspr.strategy.blade.Blade(2;0d;1d)," +
//        "jaspr.strategy.stereotype.Burnett(false),"+
//        "jaspr.strategy.stereotype.Burnett(true),"+
        " --numSimulations 10 " +
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
        "--clientInvolvementLikelihood 1 --witnessesAvailable 3 --providersAvailable 10 " +
        "--eventEffects 0 " +
        "--numRounds 200 " +
        "--memoryLimit 500 " +
        "--numSimCapabilities 5 --numProviderCapabilities 5 " +
        "--noiseRange 0d " +
        "--numTerms 5 --numAdverts 3 --numPreferences 0 " +
        "--providerAttrition 0 --clientAttrition 0").split(" ")
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
                                            numSimulations: Int = 1,
                                            clientInvolvementLikelihood: Double = 0.01,
                                            providersAvailable: Double = 0.1,
                                            witnessesAvailable: Double = 0.1,
                                            memoryLimit: Int = 100,
                                            numClients: Int = 10,
                                            numProviders: Int = 25,
                                            numSimCapabilities: Int = 10,
                                            numProviderCapabilities: Int = 5,
                                            noiseRange: Double = 1d,
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
        numSimulations = numSimulations,
        clientInvolvementLikelihood = clientInvolvementLikelihood,
        providersAvailable = providersAvailable,
        witnessesAvailable = witnessesAvailable,
        memoryLimit = memoryLimit,
        numClients = numClients,
        numProviders = numProviders,
        numSimCapabilities = numSimCapabilities,
        numProviderCapabilities = numProviderCapabilities,
        noiseRange = noiseRange,
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
                                 override val numSimulations: Int,
                                 override val clientInvolvementLikelihood: Double,
                                 val providersAvailable: Double,
                                 override val witnessesAvailable: Double,
                                 override val memoryLimit: Int,
                                 override val numClients: Int,
                                 override val numProviders: Int,
                                 val numSimCapabilities: Int,
                                 val numProviderCapabilities: Int,
                                 val noiseRange: Double,
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
      }
    })
  }

  override val baseUtility: Double = 1
  val minMean = 0
  val maxMean = 1
  val sigma = 0.05
  val fixedPreference = 1

  // Services that exist in the simulation
  lazy override val simcapabilities: Seq[ProductPayload] = {
    for (i <- 1 to numSimCapabilities) yield {
//      new ProductPayload(i.toString, (1 to numTerms).map(x => {
//        GaussianProperty(x.toString, Chooser.randomDouble(minMean, maxMean), sigma)
//      }).toList)
      new ProductPayload(i.toString, randomProperties(Chooser.randomDouble(minMean, maxMean), sigma, numTerms).toList)
    }
  }

  override def clientContext(network: Network with NetworkMarket, client: Client with Preferences, round: Int) = {
    val cap = Chooser.choose(simcapabilities).copy(
      quality = client.preferences.map(x => x._1 -> x._2.sample)
    )
    new ClientContext(client, round, cap, network.market)
  }

  override def properties(agent: Agent): SortedMap[String, Property] = {
    randomProperties(Chooser.randomDouble(minMean, maxMean), sigma, numTerms).toList
//    (1 to numTerms).map(x => GaussianProperty(x.toString, Chooser.randomDouble(minMean, maxMean), sigma)).toList
  }

  override def preferences(agent: Client): SortedMap[String, Property] = {
    if (numPreferences == 0) {
      (1 to numTerms).map(x => FixedProperty(x.toString, fixedPreference)).toList
    } else {
      Chooser.sample(randomProperties(Chooser.randomDouble(minMean, maxMean), sigma, numTerms), numPreferences).toList
    }
  }

  def randomProperties(mean: Double, sigma: Double, num: Int): Seq[Property] = {
    (1 to num).map(
      x => GaussianProperty(x.toString, Chooser.nextGaussian()*sigma + mean, sigma)
    )
  }

  def adverts(agent: Agent with Properties): SortedMap[String,Property] = {
    agent.properties.take(numAdverts).values.map(x => FixedProperty(x.name, (noiseRange*Chooser.randomDouble(minMean, maxMean)+x.doubleValue)/2d)).toList
  }

  def adverts(payload: ProductPayload, agent: Agent with Properties): SortedMap[String,Property] = {
    payload.quality.take(numAdverts).values.map(x => FixedProperty(x.name, (noiseRange*Chooser.randomDouble(minMean, maxMean)+x.doubleValue)/2d)).toList
  }


  override def toString: String = _strategy.name
}
