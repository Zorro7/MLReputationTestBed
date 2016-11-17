package jaspr.bootstrapsim.agent

import jaspr.core.service.{ServiceRequest, ServiceContext, Service}

/**
  * Created by phil on 27/09/2016.
  */
class BootService(override val request: ServiceRequest) extends Service {

  override def dependenciesSatisfied: Boolean = true

  override def canStart(currentRound: Int): Boolean = true

  override def isComplete(currentRound: Int): Boolean = {
    !isDelivered && currentRound >= end
  }

  override val serviceContext: ServiceContext = new ServiceContext
}
