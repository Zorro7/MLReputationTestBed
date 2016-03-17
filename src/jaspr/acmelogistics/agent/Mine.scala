package jaspr.acmelogistics.agent

import jaspr.acmelogistics.ACMESimulation
import jaspr.core.Simulation
import jaspr.core.agent.{Property, Provider}
import jaspr.core.provenance.Record
import jaspr.core.service.{ServiceRequest, Service}

import scala.collection.mutable.ListBuffer

/**
 * Created by phil on 17/03/16.
 */
class Mine(val simulation: ACMESimulation) extends Provider {
  override def receiveRequest(request: ServiceRequest): Boolean = ???

  override def affectService(service: Service): Unit = ???

  override def utility: Double = ???

  override def advertProperties: Map[String, Property] = ???

  override def properties: Map[String, Property] = ???

  override def gatherProvenance[T <: Record](): Seq[T] = ???

  override def getProvenance[T <: Record]: Seq[T] = ???

  override val memoryLimit: Int = simulation.config.memoryLimit
}
