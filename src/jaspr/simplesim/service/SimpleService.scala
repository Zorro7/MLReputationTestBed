package jaspr.simplesim.service

import jaspr.core.agent.Property
import jaspr.core.service.{ServiceContext, ServiceRequest, Service}

/**
 * Created by phil on 15/03/16.
 */
class SimpleService(override val request: ServiceRequest,
                    override val properties: Seq[Property]
                     ) extends Service {

  override val serviceContext: ServiceContext = new ServiceContext

  override def isComplete(currentRound: Int): Boolean = {
    !isDelivered && isStarted && currentRound >= end
  }

  override def canStart(currentRound: Int): Boolean = {
    !isDelivered && !isStarted && currentRound >= start
  }

  override def utility(): Double = {
    Math.max(0, request.properties.map(_.value).sum - properties.map(_.value).sum)
  }
}
