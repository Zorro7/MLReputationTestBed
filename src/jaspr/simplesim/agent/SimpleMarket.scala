package jaspr.simplesim.agent

import jaspr.core.Simulation
import jaspr.core.agent.Market
import jaspr.core.service.Service

/**
 * Created by phil on 17/03/16.
 */
class SimpleMarket(override val simulation: Simulation) extends Market {

  override def deliver(service: Service): Double = {
    val requested = service.request.properties.values.map(_.doubleValue).sum
    val received = service.properties.values.map(_.doubleValue).sum
    if (requested < received) {
      requested
    } else {
      0d
    }
  }

}
