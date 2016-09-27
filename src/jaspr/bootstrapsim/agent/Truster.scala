package jaspr.bootstrapsim.agent

import jaspr.core.agent.Client
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{Payload, Service, TrustAssessment, ClientContext}
import jaspr.core.simulation.Simulation

/**
  * Created by phil on 27/09/2016.
  */
class Truster(override val simulation: Simulation) extends Client {

  override def generateContext(): ClientContext = {
    new ClientContext(this, simulation.round, new Payload, new BootMarket)
  }

  override def receiveService(service: Service): Unit = {
    _utility += service.utility()
  }

  override def makeRequest(assessment: TrustAssessment): Unit = {
    assessment.request.provider.receiveRequest(assessment.request)
  }

  override def generateComposition(context: ClientContext): TrustAssessment = {
    simulation.config.strategy(this).assessReputation(simulation.network, context)
  }

  private var _utility = 0d
  override def utility: Double = _utility

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = {
    Nil
  }

  override val memoryLimit: Int = 100
}
