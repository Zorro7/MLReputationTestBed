package jaspr.sellerssim.agent

import jaspr.core.agent.Market
import jaspr.core.service.Service
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.ProductPayload

/**
 * Created by phil on 23/03/16.
 */
class SellerMarket(override val simulation: SellerSimulation) extends Market {

  override def deliver(service: Service): Double = {
//    val deliveredProduct = service.payload.asInstanceOf[ProductPayload]
//    val requestedProduct = service.request.payload.asInstanceOf[ProductPayload]
//    val delivered = deliveredProduct.quality.values.sum / deliveredProduct.quality.size.toDouble
//    val requested = requestedProduct.quality.values.sum / requestedProduct.quality.size.toDouble
//    simulation.config.baseUtility - Math.abs(delivered - requested)
    service.request.client.asInstanceOf[Buyer].rateService(service).values.sum / service.payload.asInstanceOf[ProductPayload].quality.size.toDouble
  }
}
