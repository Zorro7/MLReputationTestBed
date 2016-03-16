package jaspr.simplesim.agent

import jaspr.core.Simulation
import jaspr.core.agent.{Property, Client, Provider}
import jaspr.core.service.{TrustAssessment, ClientContext, ServiceRequest, Service}
import jaspr.simplesim.service.SimpleService
import jaspr.utilities.Chooser

import scala.collection.mutable

/**
 * Created by phil on 15/03/16.
 */
class SimpleAgent(override val simulation: Simulation) extends Client with Provider {

  private var currentUtility: Double = 0d
  override def utility = currentUtility

  override def tick(): Unit = {
    super[Client].tick()
    super[Provider].tick()
  }

  override def generateContext(): ClientContext = {
    new ClientContext(this, simulation.round)
  }

  override def receiveService(service: Service): Unit = {
    currentUtility += service.utility()
    jaspr.debug("RECEIVE:: ", service)
  }

  override def makeRequest(assessment: TrustAssessment): Unit = {
    jaspr.debug("REQUEST:: ", assessment.request)
    assessment.request.provider.receiveRequest(assessment.request)
  }

  override def generateComposition(context: ClientContext): TrustAssessment = {
    new TrustAssessment(
      new ServiceRequest(
        this,
        Chooser.choose(simulation.network.providers),
        context.round,
        1,
        Property("QOS", Chooser.randomDouble(0,3)) :: Nil
      ),
      0d
    )
  }


  override def receiveRequest(request: ServiceRequest): Boolean = {
    val service = new SimpleService(request, properties)
    currentServices += service
    true
  }

  override def affectService(service: Service): Unit = {
    
  }

  override val currentServices: mutable.ListBuffer[Service] = new mutable.ListBuffer[Service]

  override def properties: Map[String,Property] =
    Property("QOS", Chooser.randomDouble(0,3)) ::
    Nil

  override def advertProperties: Map[String,Property] = Map()
}
