package jaspr.bootstrapsim.agent

import jaspr.bootstrapsim.BootSimulation
import jaspr.core.agent.{Client, Preferences, Properties, Property}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{ClientContext, Service, TrustAssessment}

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 27/09/2016.
  */
class Truster(override val simulation: BootSimulation) extends Client with Properties with Preferences {

  override def generateContext(): ClientContext = {
    simulation.config.clientContext(this, simulation.round)
  }

  override def receiveService(service: Service): Unit = {
    jaspr.debug("RECEIVE: ", service)
    _utility += service.utility()
    trustAssessments.remove(service.request) match {
      case Some(assessment) =>
        recordProvenance(new BootRecord(assessment, service))
      case None => throw new Exception("Request " + service.request + " not found.")
    }
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
    provenance.map(_.asInstanceOf[T])
  }

  override val memoryLimit: Int = simulation.config.memoryLimit

  override val preferences: SortedMap[String,Property] = simulation.config.preferences(this)

  override val properties: SortedMap[String,Property] = simulation.config.observations(this)
}
