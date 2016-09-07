package jaspr.sellerssim.agent

import jaspr.core.agent.Client
import jaspr.core.service.{ClientContext, Service, TrustAssessment}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.{BuyerRecord, ProductPayload}

/**
 * Created by phil on 21/03/16.
 */
class Buyer(override val simulation: SellerSimulation) extends Client with Witness {

  override def generateContext(): ClientContext = {
    var context = simulation.config.clientContext(simulation.network, this, simulation.round)
    while (!simulation.network.providers.exists(_.capableOf(context.payload, 0))) {
      context = simulation.config.clientContext(simulation.network, this, simulation.round)
    }
    context
  }

  override def receiveService(service: Service): Unit = {
    jaspr.debug("RECEIVE: ", service)
    val assessment: TrustAssessment = trustAssessments.remove(service.request) match {
      case Some(x) =>
        val gain = service.utility()
        jaspr.debug(10, "UTILITY: ", simulation.round, this, utility, gain)
        _utility += gain
        x
      case None => throw new Exception("Request "+service.request+" not found.")
    }
    recordProvenance(new BuyerRecord(
      service, assessment,
      service.request.client, service.request.provider,
      service.end, service.payload, service.serviceContext.events.headOption match {
        case Some(x) => x
        case None => new SellerEvent("NA")
      },
      rateService(service)
    ))
  }


  def rateService(service: Service): Map[String,Double] = {
    val received = service.payload.asInstanceOf[ProductPayload].quality
    val wanted = service.request.payload.asInstanceOf[ProductPayload].quality
    received.map(x => x._1 -> {
//      println(wanted.get(x._1), x._2, simulation.config.baseUtility)
      wanted.get(x._1) match {
        case Some(req) => simulation.config.baseUtility - Math.abs(x._2 - req)
        case None => simulation.config.baseUtility - Math.abs(x._2)
//        case Some(req) => Math.max(simulation.config.baseUtility - Math.abs(x._2 - req), -1)
//        case None => Math.max(simulation.config.baseUtility - Math.abs(x._2), -1)
//        case Some(req) => simulation.config.baseUtility-Math.max(x._2, req)
//        case None => simulation.config.baseUtility-x._2
      }
    })
  }

  override def makeRequest(assessment: TrustAssessment): Unit = {
    jaspr.debug("REQUEST: ", assessment.request)
    assessment.request.provider.receiveRequest(assessment.request)
  }

  override def generateComposition(context: ClientContext): TrustAssessment = {
    simulation.config.strategy.assessReputation(simulation.network, context)
  }

  private var _utility: Double = 0d
  override def utility: Double = _utility

  override val memoryLimit: Int = simulation.config.memoryLimit

  override val preferences = simulation.config.preferences(this)
}

