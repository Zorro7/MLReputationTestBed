package jaspr.acmelogistics.agent

import jaspr.acmelogistics.ACMESimulation
import jaspr.core.Simulation
import jaspr.core.agent.Property
import jaspr.core.provenance.Record
import jaspr.core.service.{ServiceRequest, Service, TrustAssessment, ClientContext}

import scala.collection.mutable.ListBuffer

/**
 * Created by phil on 17/03/16.
 */
class Refinery(simulation: ACMESimulation) extends Subprovider(simulation) {
  override def affectService(performing: Service, received: Service): Unit = {}

  override def affectService(service: Service): Unit = {}

  override def makeRequest(assessment: TrustAssessment): Unit = ???

  override def utility: Double = ???

  override def advertProperties: Map[String, Property] = ???

  override def properties: Map[String, Property] = ???

  override def getProvenance[T <: Record]: Seq[T] = ???

  override val memoryLimit: Int = simulation.config.memoryLimit
}
