package jaspr.dynamicsim.agent

import jaspr.core.Simulation
import jaspr.core.agent.{Property, Provider}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{Payload, Service, ServiceRequest}
import jaspr.dynamicsim.DynamicSimulation

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 06/09/16.
  */
class DynamicProvider(override val simulation: DynamicSimulation) extends Provider {
  override def capableOf(payload: Payload, duration: Int): Boolean = ???

  override def receiveRequest(request: ServiceRequest): Boolean = ???

  override def affectService(service: Service): Unit = ???

  override def utility: Double = ???

  override def advertProperties: SortedMap[String, Property] = ???

  override val memoryLimit: Int = simulation.config.memoryLimit

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = ???

  override def properties: SortedMap[String, Property] = ???
}
