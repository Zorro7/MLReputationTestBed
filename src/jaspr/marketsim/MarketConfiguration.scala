package jaspr.marketsim

import jaspr.marketsim.agent.{MarketMarket, MarketPayload, Trustee, Trustor}
import jaspr.core.agent._
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.simulation.{Configuration, MultiConfiguration, Simulation}
import jaspr.core.strategy.Strategy
import jaspr.utilities.Chooser

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 18/01/17.
  */

object MarketMultiConfiguration extends App {

  val parser = new scopt.OptionParser[MarketMultiConfiguration]("SellerConfiguration") {
    opt[Seq[String]]("strategy") required() action { (x, c) => c.copy(strategies = x) }
    opt[Int]("numSimulations") required() action { (x, c) => c.copy(numSimulations = x) }
    opt[Int]("numRounds") required() action { (x, c) => c.copy(numRounds = x) }
    opt[Int]("numTrustees") required() action { (x, c) => c.copy(numTrustees = x) }
    opt[Int]("numTrustors") required() action { (x, c) => c.copy(numTrustors = x) }
    opt[Double]("trusteesAvailable") required() action { (x, c) => c.copy(trusteesAvailable = x) }
    opt[Double]("advisorsAvailable") required() action { (x, c) => c.copy(advisorsAvailable = x) }
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
//          "jaspr.bootstrapsim.strategy.Burnett(weka.classifiers.trees.M5P;0;2d;true;false;false;false)," + // all trustees observable
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
        "jaspr.strategy.fire.Fire(0.5d;false)," +
        "jaspr.strategy.fire.FireContext(0.5d;false)," +
        "jaspr.strategy.NoStrategy," +
        " --numSimulations 5 " +
        "--numRounds 100 " +
        "--numTrustees 100 " +
        "--numTrustors 10 " +
        "--trusteesAvailable 10 " +
        "--advisorsAvailable 10 " +
        "--trusteeLeaveLikelihood 0.05 " +
        "--trustorLeaveLikelihood 0.05 "+
        "").split(" ")
    } else args

  println(argsplt.toList mkString("[", " ", "]"))

  parser.parse(argsplt, MarketMultiConfiguration()) match {
    case Some(x) =>
      val results = Simulation(x)
      results.printChange(0, -1, _.recordsStored)
    case None =>
  }
}



case class MarketMultiConfiguration(strategies: Seq[String] = Nil,
                                  override val numSimulations: Int = 1,
                                  numRounds: Int = 50,
                                  numTrustees: Int = 100,
                                  numTrustors: Int = 10,
                                  trusteesAvailable: Double = 0.1,
                                  advisorsAvailable: Double = 1,
                                  trusteeLeaveLikelihood: Double = 0.05,
                                  trustorLeaveLikelihood: Double = 0.05
                                 ) extends MultiConfiguration {

  override val directComparison = true

  override val resultStart: Int = 0
  override val resultEnd: Int = -1
  //  override val _seed = 1

  override lazy val configs: Seq[Configuration] =
    strategies.map(x => {
      new MarketConfiguration(
        _strategy = Strategy.forName(x),
        numRounds = numRounds,
        numTrustees = numTrustees,
        numTrustors = numTrustors,
        trusteesAvailable = trusteesAvailable,
        advisorsAvailable = advisorsAvailable,
        trusteeLeaveLikelihood = trusteeLeaveLikelihood,
        trustorLeaveLikelihood = trustorLeaveLikelihood
      )
    })
}



class MarketConfiguration(val _strategy: Strategy,
                          override val numRounds: Int,
                          numTrustees: Int,
                          numTrustors: Int,
                          val trusteesAvailable: Double,
                          val advisorsAvailable: Double,
                          val trusteeLeaveLikelihood: Double,
                          val trustorLeaveLikelihood: Double
                          ) extends Configuration {

  override def toString: String = _strategy.name

  override def newSimulation(): Simulation = {
    resetSimCapabilities()
    new MarketSimulation(this)
  }

  override def strategy(agent: Client): Strategy = _strategy

  val numClients = numTrustors
  val numProviders = numTrustees
  val trustorParticipation: Double = 1
  val memoryLimit: Int = numRounds

  override val numAgents: Int = numClients + numProviders

  def clientContext(client: Client with Preferences, round: Int): ClientContext = {
    new ClientContext(client, round, Chooser.choose(simCapabilities), new MarketMarket)
  }

  def request(context: ClientContext, trustee: Trustee): ServiceRequest = {
    new ServiceRequest(
      context.client, trustee, context.round, 0, context.payload, context.market
    )
  }

  val numSimCapabilities: Int = 5
  def simCapabilities: Seq[MarketPayload] = _simCapabilities
  // Services that exist in the simulation
  private var _simCapabilities: Seq[MarketPayload] = Nil //set in newSimulation(..)
  private def resetSimCapabilities() = {
    _simCapabilities =
      (1 to numSimCapabilities).map(x =>
        new MarketPayload(x.toString, FixedProperty("a", Chooser.randomDouble(0,1)) :: Nil)
      )
  }


  def adverts(agent: Trustee): SortedMap[String, Property] = {
//    val ads: SortedMap[String,Property] = agent.properties.head._2 match {
//      case GaussianProperty(_,0.9,_) => FixedProperty("1", true) :: FixedProperty("6", true) :: Nil
//      case GaussianProperty(_,0.6,_) => FixedProperty("2", true) :: FixedProperty("4", true) :: Nil
//      case GaussianProperty(_,0.4,_) => FixedProperty("3", true) :: FixedProperty("4", true) :: Nil
//      case GaussianProperty(_,0.3,_) => FixedProperty("2", true) :: FixedProperty("3", true) :: FixedProperty("5", true) :: Nil
//      case GaussianProperty(_,0.5,_) => FixedProperty("2", true) :: FixedProperty("3", true) :: FixedProperty("6", true) :: Nil
//    }
    //    val ads: SortedMap[String,Property] = agent.properties.head._2 match {
    //      case GaussianProperty(_,0.9,_) => (1 to 2).map(x => FixedProperty(x.toString, true)).toList
    //      case GaussianProperty(_,0.6,_) => (2 to 4).map(x => FixedProperty(x.toString, true)).toList
    //      case GaussianProperty(_,0.4,_) => (4 to 6).map(x => FixedProperty(x.toString, true)).toList
    //      case GaussianProperty(_,0.3,_) => (6 to 8).map(x => FixedProperty(x.toString, true)).toList
    //      case GaussianProperty(_,0.5,_) => (8 to 10).map(x => FixedProperty(x.toString, true)).toList
    //    }
    val ads: SortedMap[String,Property] = Nil
    val fullAds: SortedMap[String,Property] = (1 to 6).map(x =>
      if (ads.contains(x.toString)) {
        ads(x.toString)
      } else {
        FixedProperty(x.toString, false)
      }
    ).toList
    fullAds
  }

  def properties(agent: Trustee): SortedMap[String, Property] = {
//    Chooser.select(
//      GaussianProperty("a", 0.9, 0.05) :: Nil,
//      GaussianProperty("a", 0.6, 0.15) :: Nil,
//      GaussianProperty("a", 0.4, 0.15) :: Nil,
//      GaussianProperty("a", 0.3, 0.05) :: Nil, //0.3,0
//      GaussianProperty("a", 0.5, 1) :: Nil //0.1 1
//    )
//    Chooser.select(
//      FixedProperty("a", 0.9) :: Nil,
//      FixedProperty("a", 0.6) :: Nil,
//      FixedProperty("a", 0.4) :: Nil,
//      FixedProperty("a", 0.3) :: Nil, //0.3,0
//      FixedProperty("a", 0.5) :: Nil //0.1 1
//    )
    FixedProperty("a", Chooser.randomDouble(0,1)) :: Nil
  }

  def preferences(agent: Trustor): SortedMap[String, Property] = {
    FixedProperty("a", 0.5) :: Nil
  }


}
