package jaspr.sellerssim.agent

import jaspr.core.agent.Client
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{Service, TrustAssessment, ClientContext}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.BuyerRecord

/**
 * Created by phil on 21/03/16.
 */
class Buyer(override val simulation: SellerSimulation) extends Client {

  override def generateContext(): ClientContext = {
    simulation.config.clientContext(simulation.network, this, simulation.round)
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
    recordProvenance(new BuyerRecord(service, assessment))
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

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = {
    if (agent == this) {
      provenance.map(_.asInstanceOf[T])
    } else {
      provenance.map(_.asInstanceOf[T]) // possibly lie
    }
  }

  override val memoryLimit: Int = simulation.config.memoryLimit
}
