package jaspr.acmelogistics.agent

import jaspr.acmelogistics.ACMESimulation
import jaspr.acmelogistics.service.ACMERecord
import jaspr.core.agent.Client
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{ClientContext, Service, TrustAssessment}

/**
 * Created by phil on 17/03/16.
 */
class ACME(override val simulation: ACMESimulation) extends Client {

  override def generateContext(): ClientContext = {
    val context = simulation.config.clientContext(simulation.network, this, simulation.round)
    jaspr.debug("generateContext: ", this, context)
    context
  }

  override def receiveService(service: Service): Unit = {
    jaspr.debug("RECEIVE: ", service)
    val assessment: TrustAssessment = trustAssessments.remove(service.request) match {
      case Some(x) =>
        val gain = service.utility()
        jaspr.debug(10, "UTILITY: ", simulation.round, this, utility, gain)
        utility += gain
        x
      case None => throw new Exception("Request "+service.request+" not found.")
    }
    recordProvenance(new ACMERecord(service, assessment))
  }

  override def makeRequest(assessment: TrustAssessment): Unit = {
    assessment.request.provider.receiveRequest(assessment.request)
  }

  override def generateComposition(context: ClientContext): TrustAssessment = {
    config.strategy(this).assessReputation(simulation.network, context)
  }

  var utility: Double = 0d

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = provenance.map(_.asInstanceOf[T])

  override val memoryLimit: Int = simulation.config.memoryLimit
}
