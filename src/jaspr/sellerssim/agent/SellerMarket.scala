package jaspr.sellerssim.agent

import jaspr.core.Simulation
import jaspr.core.agent.Market
import jaspr.core.service.Service
import jaspr.sellerssim.service.ProductPayload

/**
 * Created by phil on 23/03/16.
 */
class SellerMarket(override val simulation: Simulation) extends Market {

  override def deliver(service: Service): Double = {
    val product = service.payload.asInstanceOf[ProductPayload]
    product.quality.values.sum / product.quality.size.toDouble
  }
}
