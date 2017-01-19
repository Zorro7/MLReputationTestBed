package jaspr.bootstrapsim

import jaspr.bootstrapsim.agent.{BootMarket, BootPayload, Trustee, Truster}
import jaspr.core.agent._
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.simulation.{Configuration, MultiConfiguration, Simulation}
import jaspr.core.strategy.Strategy
import jaspr.utilities.Chooser

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 27/09/2016.
  */
object BootMultiConfiguration extends App {

  val parser = new scopt.OptionParser[BootMultiConfiguration]("SellerConfiguration") {
    opt[Seq[String]]("strategy") required() action { (x, c) => c.copy(strategies = x) }
    opt[Int]("numSimulations") required() action { (x, c) => c.copy(numSimulations = x) }
    opt[Int]("numRounds") required() action { (x, c) => c.copy(numRounds = x) }
    opt[Int]("memoryLimit") required() action { (x, c) => c.copy(memoryLimit = x) }
    opt[Int]("numTrustees") required() action { (x, c) => c.copy(numTrustees = x) }
    opt[Int]("numTrustors") required() action { (x, c) => c.copy(numTrustors = x) }
    opt[Int]("numNoiseFeatures") required() action { (x, c) => c.copy(numNoiseFeatures = x) }
    opt[Double]("observability") required() action { (x, c) => c.copy(observability = x) }
    opt[Double]("subjectivity") required() action { (x, c) => c.copy(subjectivity = x) }
    opt[Double]("trusteesAvailable") required() action { (x, c) => c.copy(trusteesAvailable = x) }
    opt[Double]("advisorsAvailable") required() action { (x, c) => c.copy(advisorsAvailable = x) }
    opt[Double]("trustorParticipation") required() action { (x, c) => c.copy(trustorParticipation = x) }
    opt[Double]("trusteeLeaveLikelihood") required() action { (x, c) => c.copy(trusteeLeaveLikelihood = x) }
    opt[Double]("trustorLeaveLikelihood") required() action { (x, c) => c.copy(trustorLeaveLikelihood = x) }
  }

//  val witnessStereotypes: Boolean = true,
//  val subjectiveStereotypes: Boolean = false,
//  val hideTrusteeIDs: Boolean = false,
//  val limitedObservations: Boolean = false

  val argsplt =
    if (args.length == 0) {
      ("--strategy " +
//        "jaspr.bootstrapsim.strategy.Burnett(weka.classifiers.trees.M5P;0;2d;true;false;false;false)," + // all trustees observable
//        "jaspr.bootstrapsim.strategy.Burnett(weka.classifiers.trees.M5P;0;2d;false;false;false;false)," + // direct stereotypes
//        "jaspr.bootstrapsim.strategy.Burnett(weka.classifiers.trees.M5P;0;2d;true;true;false;false)," + // disclosed ids
//        "jaspr.bootstrapsim.strategy.Burnett(weka.classifiers.trees.M5P;0;2d;true;true;false;true)," + // disclosed ids + limited obs
//        "jaspr.bootstrapsim.strategy.Burnett(weka.classifiers.trees.M5P;0;2d;true;true;true;false)," + // undisclosed ids
////        "jaspr.bootstrapsim.strategy.Burnett(weka.classifiers.trees.M5P;0;2d;true;true;true;true)," + // undisclosed ids + limited obs
//        "jaspr.bootstrapsim.strategy.PartialStereotype(weka.classifiers.trees.M5P;0;2d;true;false;false;false)," + // all trustees observable
//        "jaspr.bootstrapsim.strategy.PartialStereotype(weka.classifiers.trees.M5P;0;2d;true;true;false;false)," + // disclosed ids
//        "jaspr.bootstrapsim.strategy.PartialStereotype(weka.classifiers.trees.M5P;0;2d;true;true;false;true)," + // disclosed ids + limited obs
//        "jaspr.bootstrapsim.strategy.PartialStereotype(weka.classifiers.trees.M5P;0;2d;true;true;true;false)," + // undisclosed ids
//        "jaspr.bootstrapsim.strategy.PartialStereotype(weka.classifiers.trees.M5P;0;2d;true;true;true;true)," + // undisclosed ids + limited obs
        "jaspr.bootstrapsim.strategy.BRS(2d;0d)," +
//        "jaspr.bootstrapsim.strategy.BRS(0d;false;0d)," +
        "jaspr.strategy.NoStrategy," +
        " --numSimulations 5 " +
        "--numNoiseFeatures 6 " +
        "--numRounds 50 " +
        "--memoryLimit 10 " +
        "--numTrustees 100 " +
        "--numTrustors 10 " +
        "--observability 0.5 " +
        "--subjectivity 0.5 "+
        "--trusteesAvailable 10 " +
        "--advisorsAvailable 10 " +
        "--trustorParticipation 1 " +
        "--trusteeLeaveLikelihood 0.05 " +
        "--trustorLeaveLikelihood 0.05 "+
        "").split(" ")
    } else args

  println(argsplt.toList mkString("[", " ", "]"))

  parser.parse(argsplt, BootMultiConfiguration()) match {
    case Some(x) =>
      val results = Simulation(x)
      results.printChange(0, -1, _.recordsStored)
    case None =>
  }
}

case class BootMultiConfiguration(strategies: Seq[String] = Nil,
                                  override val numSimulations: Int = 1,
                                  numRounds: Int = 50,
                                  memoryLimit: Int = 50,
                                  numTrustees: Int = 100,
                                  numTrustors: Int = 10,
                                  numNoiseFeatures: Int = 0,
                                  observability: Double = 0.5,
                                  subjectivity: Double = 0.25,
                                  trusteesAvailable: Double = 0.1,
                                  advisorsAvailable: Double = 1,
                                  trustorParticipation: Double = 1,
                                  trusteeLeaveLikelihood: Double = 0.05,
                                  trustorLeaveLikelihood: Double = 0.05
                                 ) extends MultiConfiguration {
  override val directComparison = true

  override val resultStart: Int = 0
  override val resultEnd: Int = -1
//  override val _seed = 1

  override lazy val configs: Seq[Configuration] =
    strategies.map(x => {
      new BootConfiguration(
        _strategy = Strategy.forName(x),
        numRounds = numRounds,
        memoryLimit = memoryLimit,
        numTrustees = numTrustees,
        numTrustors = numTrustors,
        numNoiseFeatures = numNoiseFeatures,
        observability = observability,
        subjectivity = subjectivity,
        trusteesAvailable = trusteesAvailable,
        advisorsAvailable = advisorsAvailable,
        trustorParticipation = trustorParticipation,
        trusteeLeaveLikelihood = trusteeLeaveLikelihood,
        trustorLeaveLikelihood = trustorLeaveLikelihood
      )
    })
}


class BootConfiguration(val _strategy: Strategy,
                        override val numRounds: Int,
                        val memoryLimit: Int,
                        numTrustees: Int,
                        numTrustors: Int,
                        numNoiseFeatures: Int,
                        val observability: Double,
                        val subjectivity: Double,
                        val trusteesAvailable: Double,
                        val advisorsAvailable: Double,
                        val trustorParticipation: Double,
                        val trusteeLeaveLikelihood: Double,
                        val trustorLeaveLikelihood: Double
                       ) extends Configuration {
  override def newSimulation(): Simulation = {
    new BootSimulation(this)
  }

  override def strategy(agent: Client): Strategy = _strategy

  val numClients = numTrustors
  val numProviders = numTrustees

  override val numAgents: Int = numClients + numProviders


  def clientContext(client: Client with Preferences, round: Int): ClientContext = {
    new ClientContext(client, round, new BootPayload("stuff", quality = client.preferences), new BootMarket)
  }

  def request(context: ClientContext, provider: Provider): ServiceRequest = {
    val truster = context.client.asInstanceOf[Truster]
    val features: SortedMap[String,Property] = provider.adverts.map(x => {
      if (truster.properties.contains(x._1) && truster.properties(x._1).booleanValue) { //if it is observed and is objective
        x._2
      } else if (truster.properties.contains(x._1) && !truster.properties(x._1).booleanValue) { //if it is observed and is subjective
        FixedProperty(x._1, !x._2.booleanValue)
      } else FixedProperty(x._1, false) //if is is not observed
    }).toList
//    println(features.size, provider.generalAdverts.size, truster.properties.size, features.map(_._2.value))
    new ServiceRequest(
      context.client, provider, context.round, 0, context.payload, context.market, features
    )
  }


  def adverts(agent: Trustee): SortedMap[String, Property] = {
    val ads: SortedMap[String,Property] = agent.properties.head._2 match {
      case GaussianProperty(_,0.9,_) => FixedProperty("1", true) :: FixedProperty("6", true) :: Nil
      case GaussianProperty(_,0.6,_) => FixedProperty("2", true) :: FixedProperty("4", true) :: Nil
      case GaussianProperty(_,0.4,_) => FixedProperty("3", true) :: FixedProperty("4", true) :: Nil
      case GaussianProperty(_,0.3,_) => FixedProperty("2", true) :: FixedProperty("3", true) :: FixedProperty("5", true) :: Nil
      case GaussianProperty(_,0.5,_) => FixedProperty("2", true) :: FixedProperty("3", true) :: FixedProperty("6", true) :: Nil
    }
//    val ads: SortedMap[String,Property] = agent.properties.head._2 match {
//      case GaussianProperty(_,0.9,_) => (1 to 2).map(x => FixedProperty(x.toString, true)).toList
//      case GaussianProperty(_,0.6,_) => (2 to 4).map(x => FixedProperty(x.toString, true)).toList
//      case GaussianProperty(_,0.4,_) => (4 to 6).map(x => FixedProperty(x.toString, true)).toList
//      case GaussianProperty(_,0.3,_) => (6 to 8).map(x => FixedProperty(x.toString, true)).toList
//      case GaussianProperty(_,0.5,_) => (8 to 10).map(x => FixedProperty(x.toString, true)).toList
//    }
    val fullAds: SortedMap[String,Property] = (1 to 6).map(x =>
      if (ads.contains(x.toString)) {
        ads(x.toString)
      } else {
        FixedProperty(x.toString, false)
      }
    ).toList ++ (7 until 7+numNoiseFeatures).map(x => FixedProperty(x.toString, Chooser.nextBoolean)).toList
    fullAds
  }


  def observations(agent: Truster): SortedMap[String,Property] = {
//    val obs: SortedMap[String,Property] = Chooser.select(
//      FixedProperty("1", true) :: FixedProperty("6", true) :: Nil,
//      FixedProperty("2", true) :: FixedProperty("4", true) :: Nil,
//      FixedProperty("3", true) :: FixedProperty("4", true) :: Nil,
//      FixedProperty("2", true) :: FixedProperty("3", true) :: FixedProperty("5", true) :: Nil,
//      FixedProperty("2", true) :: FixedProperty("3", true) :: FixedProperty("6", true) :: Nil
//    ) ++
//      val obs: SortedMap[String,Property] = Chooser.select(
//        FixedProperty("1", true) :: FixedProperty("2", true) :: Nil,
//        FixedProperty("4", true) :: FixedProperty("5", true) :: Nil,
//        FixedProperty("2", true) :: FixedProperty("3", true) :: FixedProperty("4", true) :: Nil,
//        (1 to 5).map(x => FixedProperty(x.toString,true)).toList
//      )
//    val obs = (1 to 20).map(x => FixedProperty(x.toString, Chooser.randomBoolean(0.75)))
////    obs.filter(_.booleanValue).toList
//    val samplesize = (obs.size*0.5).toInt
//    Chooser.sample(obs, samplesize).toList
    val obs = (1 to 6+numNoiseFeatures).map(x => FixedProperty(x.toString, Chooser.randomBoolean(subjectivity)))
    Chooser.sample(obs, (obs.size*observability).toInt).toList
  }

  def properties(agent: Agent): SortedMap[String,Property] = {
    Chooser.select(
      GaussianProperty("a", 0.9, 0.05) :: Nil,
      GaussianProperty("a", 0.6, 0.15) :: Nil,
      GaussianProperty("a", 0.4, 0.15) :: Nil,
      GaussianProperty("a", 0.3, 0.05) :: Nil, //0.3,0
      GaussianProperty("a", 0.5, 1) :: Nil //0.1 1
    )
  }

  def preferences(agent: Agent): SortedMap[String,Property] = {
    FixedProperty("a", 0.5) :: Nil
//    Chooser.select(
//      GaussianProperty("a", 0.9, 0.05) :: Nil,
//      GaussianProperty("a", 0.6, 0.15) :: Nil,
//      GaussianProperty("a", 0.4, 0.15) :: Nil,
//      GaussianProperty("a", 0.3, 0.05) :: Nil, //0.3,0
//      GaussianProperty("a", 0.5, 1) :: Nil //0.1 1
//    )
  }


  override def toString: String = _strategy.name
}
