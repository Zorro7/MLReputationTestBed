package jaspr.bootstrapsim.agent

import jaspr.bootstrapsim.BootSimulation
import jaspr.core.agent.{Property, FixedProperty, Provider}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{ServiceRequest, Service, Payload}
import jaspr.core.simulation.Simulation

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 27/09/2016.
  */
class Trustee(override val simulation: BootSimulation) extends Provider {
  override def capableOf(payload: Payload, duration: Int): Boolean = {
    true
  }

  override def receiveRequest(request: ServiceRequest): Boolean = {
    currentServices += new BootService(request)
    true
  }

  override def affectService(service: Service): Unit = {}

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = ???

  override val memoryLimit: Int = simulation.config.memoryLimit

  override def payloadAdverts(payload: Payload): SortedMap[String, Property] = advertProperties

  override def advertProperties: SortedMap[String, Property] = simulation.config.adverts(this)

  override def properties: SortedMap[String, Property] = simulation.config.properties(this)

  override def utility: Double = ???
}
