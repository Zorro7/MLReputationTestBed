package jaspr.simplesim.agent

import jaspr.core.agent.{Client, FixedProperty, Provider}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service._
import jaspr.core.simulation.Simulation
import jaspr.simplesim.provenance.SimpleRecord
import jaspr.simplesim.service.SimpleService
import jaspr.utilities.Chooser

import scala.collection.immutable.{SortedMap, TreeMap}

/**
  * Created by phil on 15/03/16.
  */
class SimpleAgent(override val simulation: Simulation) extends Client with Provider {
  override def capableOf(payload: Payload, duration: Int): Boolean = true

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
      this, simulation.round, new Payload, SimpleMarket
    )
  }

  override def receiveService(service: Service): Unit = {
    recordProvenance(new SimpleRecord(service))
    currentUtility += service.utility()
    jaspr.debug("RECEIVE:: ", service)
  }

  override def makeRequest(assessment: TrustAssessment): Unit = {
    jaspr.debug("REQUEST:: ", assessment.request)
    assessment.request.provider.receiveRequest(assessment.request)
  }

  override def generateComposition(context: ClientContext): TrustAssessment = {
    config.strategy(this).assessReputation(simulation.network, context)
  }

  override def receiveRequest(request: ServiceRequest): Boolean = {
    val service = new SimpleService(request)
    currentServices += service
    true
  }

  override def affectService(service: Service): Unit = {
    service.duration += properties.values.map(_.intValue).sum
  }

  override val properties: SortedMap[String, FixedProperty] =
    FixedProperty("Timeliness", Chooser.randomInt(0, 2)) ::
      Nil

  override def adverts: SortedMap[String, FixedProperty] = TreeMap()

  override val memoryLimit: Int = 50

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = {
    provenance.map(_.asInstanceOf[T])
  }

}
