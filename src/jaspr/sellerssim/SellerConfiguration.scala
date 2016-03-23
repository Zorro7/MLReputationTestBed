package jaspr.sellerssim

import jaspr.core.agent.Client
import jaspr.core.service.{Payload, ClientContext}
import jaspr.core.{MultiConfiguration, Network, Simulation, Configuration}
import jaspr.core.strategy.Strategy
import jaspr.strategy.NoStrategy
import jaspr.strategy.ipaw.{IpawEvents, Ipaw}
import weka.classifiers.functions.LinearRegression

/**
 * Created by phil on 21/03/16.
 */

class SellerMultiConfiguration extends MultiConfiguration {
  override val directComparison = true

  override val _seed = 1000

  override lazy val configs: Seq[Configuration] =
      new SellerConfiguration(new NoStrategy) ::
  //    new SellerConfiguration(new RecordFire) ::
  //      new SellerConfiguration(new Fire) ::
  //    new SellerConfiguration(new Travos) ::
  //    new SellerConfiguration(new BetaReputation)::
  //    new SellerConfiguration(new Ipaw(new J48, true)) ::
//    new SellerConfiguration(new Ipaw(new LinearRegression, false)) ::
//      new SellerConfiguration(new IpawEvents(new LinearRegression, false)) ::
      //    new SellerConfiguration(new Ipaw(new OneR, true)) ::
      Nil
}

class SellerConfiguration(override val strategy: Strategy) extends Configuration {
  override def newSimulation(): Simulation = {
    new SellerSimulation(this)
  }

  override val numSimulations: Int = 1
  override val numRounds: Int = 10

  val numClients: Int = 1
  val numProviders: Int = 10

  val memoryLimit: Int = 100

  def clientContext(network: Network, client: Client, round: Int) = {
    new ClientContext(client, round, new Payload, network.markets.head)
  }
}
