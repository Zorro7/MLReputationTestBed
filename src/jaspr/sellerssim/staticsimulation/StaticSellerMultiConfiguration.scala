package jaspr.sellerssim.staticsimulation

import jaspr.core.agent._
import jaspr.core.service.{Payload, ClientContext}
import jaspr.core.simulation._
import jaspr.core.strategy.Strategy
import jaspr.sellerssim.agent._
import jaspr.sellerssim.service.ProductPayload
import jaspr.sellerssim.{SellerConfiguration, SellerNetwork, SellerSimulation}
import jaspr.utilities.Chooser

import scala.collection.immutable.{SortedMap, TreeMap}

/**
  * Created by phil on 21/03/16.
  */
object StaticSellerMultiConfiguration extends App {

  val parser = new scopt.OptionParser[StaticSellerMultiConfiguration]("SellerConfiguration") {
    opt[Seq[String]]("strategy") required() action { (x, c) => c.copy(strategies = x) }
    opt[Int]("numRounds") required() action { (x, c) => c.copy(numRounds = x) }
    opt[Int]("numSimulations") required() action { (x, c) => c.copy(numSimulations = x) }
    opt[Double]("clientInvolvementLikelihood") required() action { (x, c) => c.copy(clientInvolvementLikelihood = x) }
    opt[Int]("memoryLimit") required() action { (x, c) => c.copy(memoryLimit = x) }
    opt[Int]("numClients") required() action { (x, c) => c.copy(numClients = x) }
    opt[Int]("numProviders") required() action { (x, c) => c.copy(numProviders = x) }
    opt[Int]("numSimCapabilities") required() action { (x, c) => c.copy(numSimCapabilities = x) }
    opt[Int]("numProviderCapabilities") required() action { (x, c) => c.copy(numProviderCapabilities = x) }
    opt[Double]("noiseRange") required() action { (x, c) => c.copy(noiseRange = x) }
    opt[Int]("numTerms") required() action { (x, c) => c.copy(numTerms = x) }
    opt[Int]("numAdverts") required() action { (x, c) => c.copy(numAdverts = x) }
    opt[Boolean]("usePreferences") required() action { (x, c) => c.copy(usePreferences = x) }
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
    opt[Double]("witnessRequestLikelihood") required() action { (x, c) => c.copy(witnessRequestLikelihood = x) }
  }

  val argsplt =
    if (args.length == 0) {
      ("--strategy " +
//        "jaspr.strategy.NoStrategy," +
        "jaspr.strategy.NoStrategy," +
//        "jaspr.sellerssim.strategy.mlrs.MlrsB(weka.classifiers.bayes.NaiveBayes;2;round;250.;2.0;true;false),"+
//        "jaspr.sellerssim.strategy.mlrs.MlrsB(weka.classifiers.bayes.NaiveBayes;2;round;-1.;2.0;true;true),"+
        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.trees.RandomForest;2;0.0;true;false;false),"+
        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.trees.RandomForest;2;1.0;true;false;false),"+
        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.trees.RandomForest;2;2.0;true;false;false),"+
//        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.bayes.NaiveBayes;2;2.0;true;false;true),"+
//        "jaspr.sellerssim.strategy.mlrs.Mlrs(weka.classifiers.bayes.NaiveBayes;2;2.0;true;true;true),"+
//        "jaspr.sellerssim.strategy.general.BasicML(weka.classifiers.bayes.NaiveBayes;2),"+
//        "jaspr.sellerssim.strategy.general.BasicML(weka.classifiers.trees.RandomForest;2),"+
//        "jaspr.sellerssim.strategy.general.FireLike(weka.classifiers.trees.RandomForest;2),"+
//        "jaspr.sellerssim.strategy.general.BasicContext(weka.classifiers.bayes.NaiveBayes;2;true),"+
//        "jaspr.sellerssim.strategy.general.BasicContext(weka.classifiers.bayes.NaiveBayes;2;false),"+
//        "jaspr.sellerssim.strategy.general.BasicContext(weka.classifiers.trees.J48;2),"+
//        "jaspr.sellerssim.strategy.general.BasicContext(weka.classifiers.trees.RandomForest;2;false),"+
//        "jaspr.sellerssim.strategy.general.BasicContext(weka.classifiers.trees.RandomForest;2;true),"+
//        "jaspr.sellerssim.strategy.general.FireLikeContext(weka.classifiers.trees.RandomForest;2;false),"+
//        "jaspr.sellerssim.strategy.general.BasicContext(weka.classifiers.trees.RandomForest;2;true),"+
////        "jaspr.sellerssim.strategy.general.BasicContext(jaspr.weka.classifiers.meta.MultiRegression;2;false),"+
//        "jaspr.sellerssim.strategy.general.BasicStereotype(weka.classifiers.trees.RandomForest;2;false),"+
//        "jaspr.sellerssim.strategy.general.BasicStereotype(weka.classifiers.trees.RandomForest;2;true),"+
//        "jaspr.sellerssim.strategy.general.FireLikeStereotype(weka.classifiers.trees.RandomForest;2),"+
//        "jaspr.strategy.fire.Fire(0.0;false)," +
////        "jaspr.strategy.fire.Fire(0.0;true)," +
////        "jaspr.strategy.fire.FireContext(0.0;false)," +
////        "jaspr.strategy.fire.FireContext(0.0;true)," +
//        "jaspr.strategy.fire.Fire(0.5;false)," +
////        "jaspr.strategy.fire.Fire(0.5;true)," +
        "jaspr.strategy.betareputation.BetaReputation(0.0)," +
        "jaspr.strategy.betareputation.BetaReputation(0.5)," +
//        "jaspr.strategy.betareputation.Travos," +
        "jaspr.strategy.blade.Blade(2)," +
        "jaspr.strategy.habit.Habit(2),"+
//        "jaspr.strategy.stereotype.Burnett,"+
        " --numSimulations 5 " +
        "--honestWitnessLikelihood 1 " +
        "--pessimisticWitnessLikelihood 0 " +
        "--optimisticWitnessLikelihood 0 " +
        "--randomWitnessLikelihood 0 " +
        "--negationWitnessLikelihood 0 " +
        "--promotionWitnessLikelihood 0 " +
        "--slanderWitnessLikelihood 0 " +
        "--providersToPromote 0.25 " +
        "--providersToSlander 0.25 " +
        "--numClients 10 --numProviders 20 " +
        "--eventLikelihood 0 " +
        "--clientInvolvementLikelihood 0.1 " +
        "--eventEffects 0 " +
        "--numRounds 101 " +
        "--memoryLimit 100 " +
        "--numSimCapabilities 5 " +
        "--numProviderCapabilities 5 " +
        "--noiseRange 1.5 " +
        "--numTerms 3 " +
        "--witnessRequestLikelihood 0.5 " +
        "--numAdverts 3 " +
        "--usePreferences true").split(" ")
    } else args

  println(argsplt.toList mkString("[", " ", "]"))

  parser.parse(argsplt, StaticSellerMultiConfiguration()) match {
    case Some(x) =>
      val results = Simulation(x)
      results.printChange(0, -1, _.recordsStored)
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
//    override val _seed = 1


  override lazy val configs: Seq[Configuration] =
    strategies.map(x => {
      new StaticSellerConfiguration(
        _strategy = Strategy.forName(x),
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


class StaticSellerConfiguration(val _strategy: Strategy,
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

  override def strategy(agent: Client): Strategy = _strategy


  override val baseUtility: Double = 1d/2d



  //  val baseUtility = if (usePreferences) 2d/3d else 1d/2d
  //  val baseUtility = 1d

  def addNoise(x: Double): Double = {
    (x + Chooser.randomDouble(-1, 1)*noiseRange) / 2d
  }

  def addNoise(competency: SortedMap[String,Property], productProperty: SortedMap[String,Property]): SortedMap[String,Double] = {
    competency.map(c => c._1 -> {
      productProperty.get(c._1) match {
        case Some(x) =>
//          println(c._2.doubleValue, x.doubleValue, (c._2.doubleValue + x.doubleValue) / 2d)
//          (c._2.doubleValue + x.doubleValue) / 2d
//          addNoise((c._2.doubleValue + x.doubleValue) / 2d)
//          addNoise(c._2.doubleValue)
//          (c._2.doubleValue + noiseRange*Chooser.randomDouble(0,1))/(noiseRange+1)
          Chooser.randomDouble(0,1)
//            addNoise(x.doubleValue)
//          ((c._2.doubleValue+x.doubleValue) / 2d) * Chooser.randomDouble(-1,1)
//          (c._2.doubleValue + x.doubleValue + Chooser.randomDouble(-1,1))/3d
//         c._2.doubleValue
//        case None => c._2.doubleValue
      }
    })
  }


  def addNoise2(competency: SortedMap[String,Property], productProperty: SortedMap[String,Property]): SortedMap[String,Double] = {
    competency.map(c => c._1 -> {
      productProperty.get(c._1) match {
        case Some(x) =>
//          (c._2.doubleValue + noiseRange*Chooser.randomDouble(-1,0))/(noiseRange+1)
          Chooser.randomDouble(-1,0)
      }
    })
  }

  // Services that exist in the simulation
  lazy override val simcapabilities: Seq[ProductPayload] = {
    for (i <- 1 to numSimCapabilities) yield {
      new ProductPayload(i.toString, (1 to numTerms).map(x => {
        val y = Chooser.randomDouble(-1,1)
        new Property(x.toString, y)
      }).toList)
    }
  }

  // Services that a given provider is capable of providing - and with associated performance properties.
  def capabilities(provider: Provider): Seq[ProductPayload] = {
    var caps = Chooser.sample(simcapabilities, numProviderCapabilities)
//    val goodcaps = Chooser.sample(caps, 1).map(_.name)
//    caps = caps.map(c =>
//      if (goodcaps.contains(c.name)) {
//        c.copy(
//          quality = c.properties.map(x => x._1 -> Chooser.randomDouble(-0.5,0.5))
//        )
//      } else {
//        c.copy(
//          quality = c.properties.map(x => x._1 -> Chooser.randomDouble(0.5,1))
//        )
//      })
    if (provider.id % numProviders < 10) {
      caps = caps.map(c => c.copy(
        quality = addNoise(provider.properties, c.properties)
      ))
    } else {
      caps = caps.map(c => c.copy(
        quality = addNoise2(provider.properties, c.properties)
      ))
    }
        println(provider.name, caps)
    caps
  }

  // Context generation with required payload
  def clientContext(network: Network with NetworkMarket, client: Client with Preferences, round: Int) = {
    val cap = Chooser.choose(simcapabilities).copy(
      quality = client.preferences.map(x =>
        x._1 -> x._2.doubleValue
      )
    )
    new ClientContext(client, round, cap, network.market)
  }


  // Properties of a provider agent
  def properties(agent: Agent): SortedMap[String, Property] = {
    (1 to numTerms).map(x => new Property(x.toString, Chooser.randomDouble(-1,1))).toList
//    val base = Chooser.randomDouble(-1,1)
//    val props = for (i <- 1 to numTerms) yield {
//      val x = Chooser.nextGaussian()
//      val y = (x + base) / 2d
//      new Property(i.toString, y)
//    }
//    props.toList
  }

  // Agent preferences - the qualities of a Payload that they want to have.
  // Rayings and Utility are computed relative to this (default to 0d if the property does not exist).
  def preferences(agent: Client): SortedMap[String, Property] = {
    if (agent.id % numClients < 5) (1 to numTerms).map(x => new Property(x.toString, Chooser.randomDouble(-1d, 0))).toList
    else if (usePreferences) (1 to numTerms).map(x => new Property(x.toString, Chooser.randomDouble(-1d, 1d))).toList
    else (1 to numTerms).map(x => new Property(x.toString, 0.5)).toList
//    else (1 to numTerms).map(x => new Property(x.toString, 0d)).toList
  }

  def adverts(agent: Agent with Properties): SortedMap[String, Property] = {
    agent.properties.take(numAdverts).mapValues(x => Property(x.name, (noiseRange*Chooser.randomDouble(-1,1)+x.doubleValue)/2d))
  }

  def adverts(payload: ProductPayload, agent: Agent with Properties): List[Property] = {
    payload.quality.take(numAdverts).map(x => Property(x._1, (noiseRange*Chooser.randomDouble(-1,1)+x._2)/2d)).toList
  }


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

  override def toString: String = _strategy.name

}

