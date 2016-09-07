package jaspr.simplesim.agent

import jaspr.core.agent.Market
import jaspr.core.service.Service

/**
  * Created by phil on 17/03/16.
  */
object SimpleMarket extends Market {

  override def deliver(service: Service): Double = {
    val requested = service.request.duration
    val received = service.duration
    if (requested >= received) {
      1
    } else {
      0
    }
  }

}
