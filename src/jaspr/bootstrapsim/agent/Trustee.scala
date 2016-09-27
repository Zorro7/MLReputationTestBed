package jaspr.bootstrapsim.agent

import jaspr.core.agent.{Property, Provider}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{ServiceRequest, Service, Payload}
import jaspr.core.simulation.Simulation

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 27/09/2016.
  */
class Trustee(override val simulation: Simulation) extends Provider {
  override def capableOf(payload: Payload, duration: Int): Boolean = ???

  override def receiveRequest(request: ServiceRequest): Boolean = ???

  override def affectService(service: Service): Unit = ???

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = ???

  override val memoryLimit: Int = 100

  override def payloadAdverts(payload: Payload): SortedMap[String, Property] = ???

  override def advertProperties: SortedMap[String, Property] = ???

  override def properties: SortedMap[String, Property] = ???

  override def utility: Double = ???
}
