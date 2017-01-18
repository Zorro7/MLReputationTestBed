package jaspr.marketsim.agent

import jaspr.marketsim.MarketSimulation
import jaspr.core.agent.{Property, Provider}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{Payload, Service, ServiceRequest}

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 18/01/17.
  */
class Trustee(override val simulation: MarketSimulation) extends Provider {
  override def capableOf(payload: Payload, duration: Int): Boolean = {
    true
  }

  override def receiveRequest(request: ServiceRequest): Boolean = {
    currentServices += new MarketService(request)
    true
  }

  override def affectService(service: Service): Unit = {
    service.payload = service.payload.asInstanceOf[MarketPayload]copy(properties = properties)
  }

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = ???

  override val memoryLimit: Int = simulation.config.memoryLimit


  override val properties: SortedMap[String, Property] = simulation.config.properties(this)
  override val generalAdverts: SortedMap[String, Property] = simulation.config.adverts(this)
  override def payloadAdverts(payload: Payload): SortedMap[String, Property] = generalAdverts

  override def utility: Double = ???
}
