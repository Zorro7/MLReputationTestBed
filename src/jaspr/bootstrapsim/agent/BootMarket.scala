package jaspr.bootstrapsim.agent

import jaspr.core.agent.Market
import jaspr.core.service.Service

/**
  * Created by phil on 27/09/2016.
  */
class BootMarket extends Market {
  override def deliver(service: Service): Double = {
    println(service.payload.asInstanceOf[BootPayload].properties)
    service.payload.asInstanceOf[BootPayload].properties.values.count(x => x.doubleValue > 0.5)
  }
}
