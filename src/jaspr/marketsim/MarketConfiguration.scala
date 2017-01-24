package jaspr.marketsim

import jaspr.marketsim.agent._
import jaspr.core.agent.{FixedProperty, _}
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.simulation._
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
    opt[Double]("stereotypeFeatureNoise") required() action { (x, c) => c.copy(stereotypeFeatureNoise = x) }
    opt[Double]("contextFeatureNoise") required() action { (x, c) => c.copy(contextFeatureNoise = x) }
    opt[Int]("numPreferences") required() action { (x, c) => c.copy(numPreferences = x) }
  }

//  val classifierStr = "weka.classifiers.bayes.NaiveBayes;2"
//  val classifierStr = "weka.classifiers.trees.RandomForest;2"
  val classifierStr = "weka.classifiers.trees.M5P;0"
  val argsplt =
    if (args.length == 0) {
      ("--strategy " +
//        "jaspr.sellerssim.strategy.mlrs.Mlrs("+classifierStr+";-1d;1d;weka.classifiers.functions.LinearRegression;2.0;false;false;false;false),"+
//        "jaspr.marketsim.strategy.HabitStereotypeContextLike(2d;"+classifierStr+";-1d;1d)," +
//        "jaspr.marketsim.strategy.HabitStereotypeLike(2d;"+classifierStr+";-1d;1d)," +
//        "jaspr.marketsim.strategy.HabitContextLike(2d;"+classifierStr+";-1d;1d)," +
//        "jaspr.marketsim.strategy.HabitLike(2d;"+classifierStr+";-1d;1d)," +
//        "jaspr.marketsim.strategy.FireStereotypeContextLike(2d;"+classifierStr+";-1d;1d)," +
//        "jaspr.marketsim.strategy.FireStereotypeLike(2d;"+classifierStr+";-1d;1d)," +
//        "jaspr.marketsim.strategy.FireContextLike(2d;"+classifierStr+";-1d;1d)," +
//        "jaspr.marketsim.strategy.FireLike(2d;"+classifierStr+";-1d;1d)," +
        "jaspr.marketsim.strategy.BRSStereotypeContextLike("+classifierStr+";-1d;1d)," +
        "jaspr.marketsim.strategy.BRSStereotypeLike("+classifierStr+";-1d;1d)," +
        "jaspr.marketsim.strategy.BRSContextLike("+classifierStr+";-1d;1d)," +
        "jaspr.marketsim.strategy.BRSLike("+classifierStr+";-1d;1d)," +
//        "jaspr.marketsim.strategy.Burnett(2d;true)," +
//        "jaspr.strategy.habit.Habit(2;-1d;1d)," +
//        "jaspr.strategy.blade.Blade(2;-1d;1d)," +
//        "jaspr.marketsim.strategy.BRS(0d)," +
//        "jaspr.marketsim.strategy.BRS(0.5d)," +
//        "jaspr.marketsim.strategy.BRS(1d)," +
        "jaspr.marketsim.strategy.BRSContext(2d)," +
        "jaspr.marketsim.strategy.BRS(2d)," +
//        "jaspr.marketsim.strategy.Fire(0d)," +
        "jaspr.marketsim.strategy.FireContext(0.5d)," +
        "jaspr.marketsim.strategy.Fire(0.5d)," +
//        "jaspr.marketsim.strategy.Fire(1d)," +
//        "jaspr.marketsim.strategy.Fire(2d)," +
        "jaspr.strategy.NoStrategy," +
        " --numSimulations 5 " +
        "--numRounds 100 " +
        "--numTrustees 100 " +
        "--numTrustors 10 " +
        "--trusteesAvailable 10 " +
        "--advisorsAvailable 5 " +
        "--trusteeLeaveLikelihood 0.01 " +
        "--trustorLeaveLikelihood 0.01 "+
        "--stereotypeFeatureNoise 0.0 "+
        "--contextFeatureNoise 0.0 "+
        "--numPreferences 1 " +
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
                                  trustorLeaveLikelihood: Double = 0.05,
                                  stereotypeFeatureNoise: Double = 0d,
                                  contextFeatureNoise: Double = 0d,
                                  numPreferences: Int = 1
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
        trustorLeaveLikelihood = trustorLeaveLikelihood,
        stereotypeFeatureNoise = stereotypeFeatureNoise,
        contextFeatureNoise = contextFeatureNoise,
        numPreferences = numPreferences
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
                          val trustorLeaveLikelihood: Double,
                          val stereotypeFeatureNoise: Double,
                          val contextFeatureNoise: Double,
                          val numPreferences: Double
                          ) extends Configuration {

  override def toString: String = _strategy.name

  override def newSimulation(): Simulation = {
    resetSimCapabilities()
    new MarketSimulation(this)
  }

  override def strategy(agent: Client): Strategy = _strategy

  val numClients: Int = numTrustors
  val numProviders: Int = numTrustees
  val trustorParticipation: Double = 1
  val memoryLimit: Int = numRounds

  override val numAgents: Int = numClients + numProviders

  def clientContext(client: Trustor, round: Int) = {
    val cap = Chooser.choose(simCapabilities).copy(
      properties = client.preferences.map(x => x._1 -> x._2.sample)
    )
    new ClientContext(client, round, cap, new MarketMarket)
  }

  def request(context: ClientContext, trustee: Trustee): ServiceRequest = {
    new ServiceRequest(
      context.client, trustee, context.round, 0, context.payload, context.market
    )
  }

  def witnessModel(witness: Witness, network: Network): WitnessModel = {
//    new ObjectiveWitnessModel
    Chooser.choose(
      new HonestWitnessModel ::
      new NegationWitnessModel ::
      Nil,
      1d :: 0d :: Nil
    )
  }

  val numSimCapabilities: Int = 5
  def simCapabilities: Seq[MarketPayload] = _simCapabilities
  // Services that exist in the simulation
  private var _simCapabilities: Seq[MarketPayload] = Nil //set in newSimulation(..)
  private def resetSimCapabilities() = {

    _simCapabilities =
      (1 to numSimCapabilities).map(x => {
//        val properties = Chooser.select(
//          GaussianProperty("a", 0.4, 0.05),
//          GaussianProperty("a", 0.3, 0.5), //asdf
//          GaussianProperty("a", 0.1, 0.15),
//          GaussianProperty("a", -0.1, 0.15),
//          GaussianProperty("a", -0.2, 0.05), //0.3,0
//          GaussianProperty("a", -0.3, 0.5), //asdf
//          GaussianProperty("a", 0, 1) //0.1 1
//        ) :: Nil
        val properties = GaussianProperty(
          "a",
          Chooser.choose(Range.Double.inclusive(-0.5d,0.5d,0.1)),
          Chooser.choose(Range.Double.inclusive(0.5d,0.5d,0.1))
        ) :: Nil
        val ads: SortedMap[String,Property] = properties.head match {
          case GaussianProperty(_,-0.5,_) => (1 :: 2 :: Nil).map(x => FixedProperty(x.toString, true))
          case GaussianProperty(_,-0.4,_) => (1 :: 2 :: 3 :: Nil).map(x => FixedProperty(x.toString, true))
          case GaussianProperty(_,-0.3,_) => (2 :: 3 :: 4 :: Nil).map(x => FixedProperty(x.toString, true))
          case GaussianProperty(_,-0.2,_) => (1 :: 3 :: 5 :: Nil).map(x => FixedProperty(x.toString, true))
          case GaussianProperty(_,-0.1,_) => (4 :: 5 :: 6 :: Nil).map(x => FixedProperty(x.toString, true))
          case GaussianProperty(_,0.0,_) => (2 :: 5 :: 7 :: Nil).map(x => FixedProperty(x.toString, true))
          case GaussianProperty(_,0.1,_) => (4 :: 6 :: 8 :: Nil).map(x => FixedProperty(x.toString, true))
          case GaussianProperty(_,0.2,_) => (6 :: 7 :: 8 :: Nil).map(x => FixedProperty(x.toString, true))
          case GaussianProperty(_,0.3,_) => (5 :: 7 :: 9 :: Nil).map(x => FixedProperty(x.toString, true))
          case GaussianProperty(_,0.4,_) => (5 :: 6 :: 8 :: Nil).map(x => FixedProperty(x.toString, true))
          case GaussianProperty(_,0.5,_) => (8 :: 9 :: 10 :: Nil).map(x => FixedProperty(x.toString, true))
        }
        val adverts = (1 to 10).map(x =>
          if (Chooser.randomBoolean(contextFeatureNoise)) {
            FixedProperty(x.toString, Chooser.randomBoolean(0.5))
          } else if (ads.contains(x.toString)) {
            ads(x.toString)
          } else {
            FixedProperty(x.toString, false)
          }
        ).toList
        new MarketPayload(x.toString, properties, adverts)
      })
  }


  def adverts(agent: Trustee): SortedMap[String, Property] = {
    val ads: SortedMap[String,Property] = agent.properties.head._2 match {
      case GaussianProperty(_,0.0,_) => (1 :: 2 :: Nil).map(x => FixedProperty(x.toString, true))
      case GaussianProperty(_,0.1,_) => (1 :: 2 :: 3 :: Nil).map(x => FixedProperty(x.toString, true))
      case GaussianProperty(_,0.2,_) => (2 :: 3 :: 4 :: Nil).map(x => FixedProperty(x.toString, true))
      case GaussianProperty(_,0.3,_) => (1 :: 3 :: 5 :: Nil).map(x => FixedProperty(x.toString, true))
      case GaussianProperty(_,0.4,_) => (4 :: 5 :: 6 :: Nil).map(x => FixedProperty(x.toString, true))
      case GaussianProperty(_,0.5,_) => (2 :: 5 :: 7 :: Nil).map(x => FixedProperty(x.toString, true))
      case GaussianProperty(_,0.6,_) => (4 :: 6 :: 8 :: Nil).map(x => FixedProperty(x.toString, true))
      case GaussianProperty(_,0.7,_) => (6 :: 7 :: 8 :: Nil).map(x => FixedProperty(x.toString, true))
      case GaussianProperty(_,0.8,_) => (5 :: 7 :: 9 :: Nil).map(x => FixedProperty(x.toString, true))
      case GaussianProperty(_,0.9,_) => (5 :: 6 :: 8 :: Nil).map(x => FixedProperty(x.toString, true))
      case GaussianProperty(_,1.0,_) => (8 :: 9 :: 10 :: Nil).map(x => FixedProperty(x.toString, true))
    }
    val fullAds: SortedMap[String,Property] = (1 to 10).map(x =>
      if (Chooser.randomBoolean(stereotypeFeatureNoise)) {
        FixedProperty(x.toString, Chooser.randomBoolean(0.5))
      } else if (ads.contains(x.toString)) {
        ads(x.toString)
      } else {
        FixedProperty(x.toString, false)
      }
    ).toList
    fullAds
  }

  def properties(agent: Trustee): SortedMap[String, Property] = {
//    Chooser.select(
//      GaussianProperty("a", 0.9, 0.05),
//      GaussianProperty("a", 0.8, 0.5), //asdf
//      GaussianProperty("a", 0.6, 0.15),
//      GaussianProperty("a", 0.4, 0.15),
//      GaussianProperty("a", 0.3, 0.05), //0.3,0
//      GaussianProperty("a", 0.2, 0.5), //asdf
//      GaussianProperty("a", 0.5, 1) //0.1 1
//    ) :: Nil
    GaussianProperty(
      "a",
      Chooser.choose(Range.Double.inclusive(0d,0.9d,0.1)),
      Chooser.choose(Range.Double.inclusive(0.1d,0.5d,0.1))
    ) :: Nil
  }

  def capabilities(agent: Trustee): Seq[MarketPayload] = {
    val caps = simCapabilities.map(cap =>
      cap.copy(properties = agent.properties.map(x => {
        val prop: GaussianProperty = x._2.asInstanceOf[GaussianProperty]
//        prop.name -> GaussianProperty(prop.name, prop.mean + Chooser.randomGaussian(0,0.5), prop.std)
        prop.name -> GaussianProperty(prop.name, prop.mean + cap.properties(x._1).doubleValue, prop.std)
//        prop.name -> FixedProperty(prop.name, prop.value)
      }))
//      cap.copy(properties = properties(agent))
    )
//    println(agent.properties)
//    println("\t", simCapabilities)
//    println("\t", caps)
//    println()
    caps
  }

  def preferences(agent: Trustor): SortedMap[String, Property] = {
    if (numPreferences <= 1) FixedProperty("a", 0.5) :: Nil
    else FixedProperty("a", Chooser.choose(Range.Double.inclusive(0d,1d,1/numPreferences.toDouble))) :: Nil
  }

}
