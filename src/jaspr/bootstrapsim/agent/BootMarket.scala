package jaspr.bootstrapsim.agent

import jaspr.core.agent.Market
import jaspr.core.service.Service

/**
  * Created by phil on 27/09/2016.
  */
class BootMarket extends Market {
  override def deliver(service: Service): Double = {
    val delivered = service.payload.asInstanceOf[BootPayload]
    val requested = service.request.payload.asInstanceOf[BootPayload]
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
