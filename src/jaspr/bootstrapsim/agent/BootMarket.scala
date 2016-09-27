package jaspr.bootstrapsim.agent

import jaspr.core.agent.Market
import jaspr.core.service.Service

/**
  * Created by phil on 27/09/2016.
  */
class BootMarket extends Market {
  override def deliver(service: Service): Double = {
    1d
  }
}
