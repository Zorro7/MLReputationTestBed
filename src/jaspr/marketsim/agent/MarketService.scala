package jaspr.marketsim.agent

import jaspr.core.service.{Service, ServiceContext, ServiceRequest}

/**
  * Created by phil on 18/01/17.
  */
class MarketService(override val request: ServiceRequest) extends Service {

  override def dependenciesSatisfied: Boolean = true

  override def canStart(currentRound: Int): Boolean = true

  override def isComplete(currentRound: Int): Boolean = {
    !isDelivered && currentRound >= end
  }

  override val serviceContext: ServiceContext = new ServiceContext
}
