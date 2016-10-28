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
  }

  val argsplt =
    if (args.length == 0) {
      ("--strategy " +
        "jaspr.bootstrapsim.strategy.PartialStereotype(weka.classifiers.trees.M5P;0;2d;false;true;false;0d)," +
        "jaspr.bootstrapsim.strategy.JasprStereotype(weka.classifiers.trees.M5P;0;2d;false;true;false;false;0d)," +
        "jaspr.bootstrapsim.strategy.Burnett(weka.classifiers.trees.M5P;0;2d;false;true;false;false;0d)," +
        "jaspr.bootstrapsim.strategy.Burnett(weka.classifiers.trees.M5P;0;2d;false;true;false;true;0d)," +
//        "jaspr.bootstrapsim.strategy.Burnett(weka.classifiers.trees.M5P;0;2d;true;true;false;false;0d)," +
        "jaspr.bootstrapsim.strategy.Burnett(weka.classifiers.trees.M5P;0;2d;false;false;false;false;0d)," +
        "jaspr.bootstrapsim.strategy.Burnett(weka.classifiers.trees.M5P;0;0d;false;false;false;false;0d)," +
//        "jaspr.bootstrapsim.strategy.BRS(2d;true;0d)," +
        "jaspr.bootstrapsim.strategy.BRS(2d;false;0d)," +
        "jaspr.bootstrapsim.strategy.BRS(0d;false;0d)," +
        "jaspr.strategy.NoStrategy," +
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

case class BootMultiConfiguration(strategies: Seq[String] = Nil) extends MultiConfiguration {
  override val directComparison = true

  override val resultStart: Int = 0
  override val resultEnd: Int = -1
//  override val _seed = 1

  override lazy val configs: Seq[Configuration] =
    strategies.map(x => {
      new BootConfiguration(
        _strategy = Strategy.forName(x)
      )
    })
}


class BootConfiguration(val _strategy: Strategy) extends Configuration {
  override def newSimulation(): Simulation = {
    new BootSimulation(this)
  }

  override def strategy(agent: Client): Strategy = _strategy

  override val numSimulations: Int = 5
  val numClients = 5
  val numProviders = 100

  val trusteeLeaveLikelihood = 0.05
  val trusterLeaveLikelihood = 0.05
  val trusteeAvailableLikleihood = 0.1
  val trusterParticipationLikelihood = 1
  val witnessRequestLikelihood =  1

  override val numAgents: Int = numClients + numProviders
  override val numRounds: Int = 100
  val memoryLimit: Int = 100


  def clientContext(client: Client with Preferences, round: Int): ClientContext = {
    new ClientContext(client, round, new BootPayload("stuff", quality = client.preferences), new BootMarket)
  }

  def request(context: ClientContext, provider: Provider): ServiceRequest = {
    val truster = context.client.asInstanceOf[Truster]
    val features: SortedMap[String,Property] = provider.generalAdverts.map(x => {
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
//    val ads: SortedMap[String,Property] = agent.properties.head._2 match {
//      case GaussianProperty(_,0.9,_) => FixedProperty("1", true) :: FixedProperty("5", true) :: Nil
//      case GaussianProperty(_,0.6,_) => FixedProperty("2", true) :: FixedProperty("4", true) :: Nil
//      case GaussianProperty(_,0.4,_) => FixedProperty("3", true) :: FixedProperty("4", true) :: Nil
//      case GaussianProperty(_,0.3,_) => FixedProperty("2", true) :: FixedProperty("3", true) :: Nil
//      case GaussianProperty(_,0.5,_) => FixedProperty("1", true) :: FixedProperty("2", true) :: Nil
//    }
    val ads: SortedMap[String,Property] = agent.properties.head._2 match {
      case GaussianProperty(_,0.9,_) => (1 to 2).map(x => FixedProperty(x.toString, true)).toList
      case GaussianProperty(_,0.6,_) => (2 to 4).map(x => FixedProperty(x.toString, true)).toList
      case GaussianProperty(_,0.4,_) => (4 to 6).map(x => FixedProperty(x.toString, true)).toList
      case GaussianProperty(_,0.3,_) => (6 to 8).map(x => FixedProperty(x.toString, true)).toList
      case GaussianProperty(_,0.5,_) => (8 to 10).map(x => FixedProperty(x.toString, true)).toList
    }
    val fullAds: SortedMap[String,Property] = (1 to 10).map(x =>
      if (ads.contains(x.toString)) {
        ads(x.toString)
      } else {
        FixedProperty(x.toString, false)
      }
    ).toList //++ (16 to 20).map(x => FixedProperty(x.toString, Chooser.nextBoolean)).toList
    fullAds
  }

  val observability = 0.5
  val subjectivity = 0.25
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
    val obs = (1 to 10).map(x => FixedProperty(x.toString, Chooser.randomBoolean(subjectivity)))
    Chooser.sample(obs, (obs.size*observability).toInt).toList
//    obs
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
//    Chooser.ifHappens(0.5)(FixedProperty("a", 0.1) :: Nil)(FixedProperty("a", 0.9) :: Nil)
  }


  override def toString: String = _strategy.name
}
