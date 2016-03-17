package jaspr.acmelogistics.agent

import jaspr.acmelogistics.ACMESimulation
import jaspr.core.agent.Client
import jaspr.core.provenance.Record
import jaspr.core.service.{Service, TrustAssessment, ClientContext}

/**
 * Created by phil on 17/03/16.
 */
class ACME(val simulation: ACMESimulation) extends Client {

  override def generateContext(): ClientContext = ???

  override def receiveService(service: Service): Unit = ???

  override def makeRequest(assessment: TrustAssessment): Unit = ???

  override def generateComposition(context: ClientContext): TrustAssessment = ???

  override def utility: Double = ???

  override def gatherProvenance[T <: Record](): Seq[T] = ???

  override def getProvenance[T <: Record]: Seq[T] = ???

  override val memoryLimit: Int = simulation.config.memoryLimit
}
