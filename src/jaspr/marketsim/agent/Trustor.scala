package jaspr.marketsim.agent

import jaspr.core.agent.{Client, Preferences, Property}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{ClientContext, Service, TrustAssessment}
import jaspr.marketsim.MarketSimulation

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 18/01/17.
  */
class Trustor(override val simulation: MarketSimulation) extends Client with Preferences with Witness {

  override def generateContext(): ClientContext = {
    simulation.config.clientContext(this, simulation.round)
  }

  override def receiveService(service: Service): Unit = {
    jaspr.debug("RECEIVE: ", service)
    _utility += service.utility()
    trustAssessments.remove(service.request) match {
      case Some(assessment) =>
        recordProvenance(new MarketRecord(assessment, service))
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

}
