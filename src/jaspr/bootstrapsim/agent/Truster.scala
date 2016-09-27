package jaspr.bootstrapsim.agent

import jaspr.core.agent.Client
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{Service, TrustAssessment, ClientContext}
import jaspr.core.simulation.Simulation

/**
  * Created by phil on 27/09/2016.
  */
class Truster(override val simulation: Simulation) extends Client {
  override def generateContext(): ClientContext = ???

  override def receiveService(service: Service): Unit = ???

  override def makeRequest(assessment: TrustAssessment): Unit = ???

  override def generateComposition(context: ClientContext): TrustAssessment = ???

  override def utility: Double = ???

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = ???

  override val memoryLimit: Int = 100
}
