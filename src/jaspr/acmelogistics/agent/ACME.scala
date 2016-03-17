package jaspr.acmelogistics.agent

import jaspr.acmelogistics.ACMESimulation
import jaspr.acmelogistics.service.ACMERecord
import jaspr.core.agent.Client
import jaspr.core.provenance.{ServiceRecord, Record}
import jaspr.core.service.{ServiceRequest, Service, TrustAssessment, ClientContext}

import scala.collection.mutable

/**
 * Created by phil on 17/03/16.
 */
class ACME(val simulation: ACMESimulation) extends Client {

  override def generateContext(): ClientContext = {
    val context = simulation.config.clientContext(simulation.network, this, simulation.round)
    jaspr.debug("generateContext: ", this, context)
    context
  }



  override def receiveService(service: Service): Unit = {
    jaspr.debug("RECEIVE: ", this, service)
    val assessment: TrustAssessment = trustAssessments.remove(service.request) match {
      case Some(x) =>
        val gain = service.utility()
        jaspr.debug(10, "UTILITY: ", simulation.round.toString, this, utility, gain)
        utility += gain
        x
      case None => throw new Exception("Request "+service.request+" not found.")
    }
    recordProvenance(new ACMERecord(service, assessment))
  }

  override def makeRequest(assessment: TrustAssessment): Unit = ???

  override def generateComposition(context: ClientContext): TrustAssessment = {
    config.strategy.assessReputation(simulation.network, context)
  }

  var utility: Double = 0d

  override def getProvenance[T <: Record]: Seq[T] = {
    provenance.map(_.asInstanceOf[T])
  }

  override def gatherProvenance[T <: Record](): Seq[T] = {
    simulation.network.agents.withFilter(_ != this).flatMap(x => x.getProvenance[T])
  }

  override val memoryLimit: Int = simulation.config.memoryLimit
}
