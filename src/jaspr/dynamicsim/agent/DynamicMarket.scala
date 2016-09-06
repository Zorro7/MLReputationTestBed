package jaspr.dynamicsim.agent

import jaspr.core.Simulation
import jaspr.core.agent.Market
import jaspr.core.service.Service

/**
  * Created by phil on 06/09/16.
  */
class DynamicMarket(override val simulation: Simulation) extends Market {

  override def deliver(service: Service): Double = {
    1d
  }
}
