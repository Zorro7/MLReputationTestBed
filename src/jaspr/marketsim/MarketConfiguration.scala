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
        "--numRounds 50 " +
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
    new MarketSimulation(this)
  }

  override def strategy(agent: Client): Strategy = _strategy

  val numClients = numTrustors
  val numProviders = numTrustees
  val trustorParticipation: Double = 1
  val memoryLimit: Int = numRounds

  override val numAgents: Int = numClients + numProviders

  def clientContext(client: Client with Preferences, round: Int): ClientContext = {
    new ClientContext(client, round, new MarketPayload("stuff", quality = client.preferences), new MarketMarket)
  }

  def request(context: ClientContext, trustee: Trustee): ServiceRequest = {
    new ServiceRequest(
      context.client, trustee, context.round, 0, context.payload, context.market
    )
  }

  def adverts(agent: Trustee): SortedMap[String, Property] = {
    Nil
  }

  def properties(agent: Trustee): SortedMap[String, Property] = {
    GaussianProperty("a", Chooser.randomDouble(0,1), 0.1) :: Nil
  }

  def preferences(agent: Trustor): SortedMap[String, Property] = {
    GaussianProperty("a", 0.5, 0.1) :: Nil
  }


}
