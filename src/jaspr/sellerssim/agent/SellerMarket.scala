package jaspr.sellerssim.agent

import jaspr.core.Simulation
import jaspr.core.agent.Market
import jaspr.core.service.Service

/**
 * Created by phil on 23/03/16.
 */
class SellerMarket(override val simulation: Simulation) extends Market {

  override def deliver(service: Service): Double = {
    1d
  }
}
