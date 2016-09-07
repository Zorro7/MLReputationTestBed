package jaspr.simplesim.agent

import jaspr.core.agent.Market
import jaspr.core.service.Service
import jaspr.core.simulation.Simulation

/**
 * Created by phil on 17/03/16.
 */
class SimpleMarket(override val simulation: Simulation) extends Market {

  override def deliver(service: Service): Double = {
    val requested = service.request.duration
    val received = service.duration
    println(requested, received)
    if (requested >= received) {
      1
    } else {
      0
    }
  }

}
