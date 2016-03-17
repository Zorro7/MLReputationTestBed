package jaspr.acmelogistics.service

import jaspr.core.agent.{Provider, Client, Property}
import jaspr.core.service.{ClientContext, ServiceRequest, ServiceContext, Service}

/**
 * Created by phil on 17/03/16.
 */
class ACMEService(override val request: ServiceRequest) extends Service {

  override def isComplete(currentRound: Int): Boolean = {
    !isDelivered && isStarted && currentRound >= end
  }

  override def canStart(currentRound: Int): Boolean = {
    !isDelivered && !isStarted && currentRound >= start
  }

  override val serviceContext: ServiceContext = new ServiceContext
}