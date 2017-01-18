package jaspr.sellerssim.agent

import jaspr.core.agent.Market
import jaspr.core.service.Service
import jaspr.sellerssim.service.ProductPayload

/**
  * Created by phil on 23/03/16.
  */
class SellerMarket extends Market {

  override def deliver(service: Service): Double = {
    //    val deliveredProduct = service.payload.asInstanceOf[ProductPayload]
    //    val requestedProduct = service.request.payload.asInstanceOf[ProductPayload]
    //    val delivered = deliveredProduct.quality.values.sum / deliveredProduct.quality.size.toDouble
    //    val requested = requestedProduct.quality.values.sum / requestedProduct.quality.size.toDouble
    //    simulation.config.baseUtility - Math.abs(delivered - requested)
//    service.request.client.asInstanceOf[Buyer].rateService(service).values.sum / service.payload.asInstanceOf[ProductPayload].quality.size.toDouble
    val delivered = service.payload.asInstanceOf[ProductPayload]
    val requested = service.request.payload.asInstanceOf[ProductPayload]
    val disparity = requested.quality.map(r =>
      delivered.quality.get(r._1) match {
        case Some(d) => {
          d.doubleValue - r._2.doubleValue
        }
        case None => 0
      }
    )
    disparity.count(_ > 0) / disparity.size.toDouble
  }
}
