package jaspr.acmelogistics.service

import jaspr.core.service.{Service, ServiceContext, ServiceRequest}

/**
  * Created by phil on 17/03/16.
  */
class ACMEService(override val request: ServiceRequest) extends Service {

  override def isComplete(currentRound: Int): Boolean = {
    !isDelivered && isStarted && currentRound >= end && dependenciesSatisfied
  }

  override def canStart(currentRound: Int): Boolean = {
    !isDelivered && !isStarted && currentRound >= start && dependenciesSatisfied
  }

  override def dependenciesSatisfied = request.dependencies.forall(_.isDelivered)

  override val serviceContext: ServiceContext = new ServiceContext
}