package jaspr.simplesim.agent

import jaspr.core.Simulation
import jaspr.core.agent.{Property, Client, Provider}
import jaspr.core.provenance.Record
import jaspr.core.service.{TrustAssessment, ClientContext, ServiceRequest, Service}
import jaspr.simplesim.provenance.SimpleRecord
import jaspr.simplesim.service.SimpleService
import jaspr.strategy.NoStrategy
import jaspr.utilities.Chooser

import scala.collection.mutable

/**
 * Created by phil on 15/03/16.
 */
class SimpleAgent(override val simulation: Simulation) extends Client with Provider {

  private var currentUtility: Double = 0d
  override def utility = currentUtility

  override def tick(): Unit = {
    clientTick()
    providerTick()
  }

  def providerTick() = super[Provider].tick()
  def clientTick() = super[Client].tick()

  override def generateContext(): ClientContext = {
    new ClientContext(
      this, simulation.round, simulation.network.markets.head, Property("QOS", Chooser.randomDouble(0,3)) :: Nil
    )
  }

  override def receiveService(service: Service): Unit = {
    recordProvenance(new SimpleRecord(service, service.properties))
    currentUtility += service.utility()
    jaspr.debug("RECEIVE:: ", service)
  }

  override def makeRequest(assessment: TrustAssessment): Unit = {
    jaspr.debug("REQUEST:: ", assessment.request)
    assessment.request.provider.receiveRequest(assessment.request)
  }

  override def generateComposition(context: ClientContext): TrustAssessment = {
    config.strategy.assessReputation(simulation.network, context)
  }

  override def receiveRequest(request: ServiceRequest): Boolean = {
    val service = new SimpleService(request, properties)
    currentServices += service
    true
  }

  override def affectService(service: Service): Unit = {}

  override val properties: Map[String,Property] =
    Property("QOS", Chooser.randomDouble(0,3)) ::
    Nil

  override def advertProperties: Map[String,Property] = Map()




  override val memoryLimit: Int = 50

  override def getProvenance[T <: Record]: Seq[T] = {
    provenance.map(_.asInstanceOf[T])
  }

  override def gatherProvenance[T <: Record](): Seq[T] = {
    simulation.network.agents.withFilter(_ != this).flatMap(_.getProvenance)
  }

}
