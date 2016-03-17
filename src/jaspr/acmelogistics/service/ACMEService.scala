package jaspr.acmelogistics.service

import jaspr.core.agent.{Provider, Client, Property}
import jaspr.core.service.{ClientContext, ServiceRequest, ServiceContext, Service}

/**
 * Created by phil on 17/03/16.
 */
class ACMEService(override val request: ServiceRequest,
                  override val properties: Map[String, Property]
                   ) extends Service {

  override def canStart(currentRound: Int): Boolean = ???

  override def isComplete(currentRound: Int): Boolean = ???

  override val serviceContext: ServiceContext = new ServiceContext
}
