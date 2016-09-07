package jaspr.sellerssim.service

import jaspr.core.service.{Service, ServiceContext, ServiceRequest}

/**
  * Created by phil on 23/03/16.
  */
class SellerService(override val request: ServiceRequest) extends Service {

  override def dependenciesSatisfied: Boolean = true

  override def canStart(currentRound: Int): Boolean = true

  override def isComplete(currentRound: Int): Boolean = {
    !isDelivered && currentRound >= end
  }

  override val serviceContext: ServiceContext = new ServiceContext
}
