package jaspr.sellerssim

import jaspr.core.results.Result
import jaspr.core.simulation.{Network, NetworkMarket, Simulation}
import jaspr.sellerssim.staticsimulation.StaticSellerMultiConfiguration
import jaspr.utilities.{Chooser, Tickable}

/**
  * Created by phil on 21/03/16.
  */
object SellerSimulation extends App {
  val results = Simulation(new StaticSellerMultiConfiguration)
}

class SellerSimulation(val config: SellerConfiguration) extends Simulation {

  override val network: Network with NetworkMarket = config.network(this)

  override def act(): Result = {
    network match {
      case x: Tickable => x.tick()
      case _ => // do nothing
    }

    for (client <- network.clients) {
      Chooser.ifHappens(config.clientInvolvementLikelihood)(
        client.tick()
      )()
    }

    for (provider <- network.providers) {
      provider.tick()
    }

    new Result(this)
  }

  override def run(): List[Result] = {
    val results = super.run()

    for (client <- network.clients) {
      val context = client.generateContext()
      val assessment = config.strategy(client).assessReputation(network, context)
      assessment.request.provider.receiveRequest(assessment.request)
      assessment.request.provider.tryStartServices()
      for (service <- assessment.request.provider.currentServices) {
        service.tryStartService(round)
        service.tryEndService(round)
        service.utility()
        println(client.id, assessment.request.provider.id, service.utility(), assessment.trustValue)
      }


    }

    results
  }

}
