package jaspr.simplesim.service

import jaspr.core.service.{ServiceContext, ServiceRequest, Service}

/**
 * Created by phil on 15/03/16.
 */
class SimpleService(override val request: ServiceRequest) extends Service {

  override val serviceContext: ServiceContext = new ServiceContext

  override def isComplete(currentRound: Int): Boolean = {
    !isDelivered && isStarted && currentRound >= end
  }

  override def canStart(currentRound: Int): Boolean = {
    !isDelivered && !isStarted && currentRound >= start
  }
}
