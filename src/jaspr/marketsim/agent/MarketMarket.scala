package jaspr.marketsim.agent

import jaspr.core.agent.Market
import jaspr.core.service.Service

/**
  * Created by phil on 27/09/2016.
  */
class MarketMarket extends Market {

  override def deliver(service: Service): Double = {
    val delivered = service.payload.asInstanceOf[MarketPayload]
    delivered.properties.map(_._2.doubleValue).sum / delivered.properties.size.toDouble
//    val requested = service.request.payload.asInstanceOf[MarketPayload]
////    println(delivered.quality, service.request.provider.properties, requested.quality)
//    val disparity = requested.properties.map(r =>
//      delivered.properties.get(r._1) match {
//        case Some(d) => d.doubleValue - r._2.doubleValue
//        case None => 0
//      }
//    )
//    disparity.count(_ >= 0) / disparity.size.toDouble
  }
}
