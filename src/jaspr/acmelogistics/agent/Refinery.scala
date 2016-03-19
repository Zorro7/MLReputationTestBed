package jaspr.acmelogistics.agent

import jaspr.acmelogistics.ACMESimulation
import jaspr.acmelogistics.service.GoodPayload
import jaspr.core.Simulation
import jaspr.core.agent.Property
import jaspr.core.provenance.Record
import jaspr.core.service.{ServiceRequest, Service, TrustAssessment, ClientContext}

import scala.collection.mutable.ListBuffer

/**
 * Created by phil on 17/03/16.
 */
class Refinery(simulation: ACMESimulation) extends Subprovider(simulation) {
  override def affectService(performing: Service, received: Service): Unit = {
    performing.payload =
      if (received.payload.asInstanceOf[GoodPayload].quality < this.property("OrePurityReq").doubleValue) {
        performing.payload.asInstanceOf[GoodPayload].copy(quantity = 0d)
      } else {
        performing.payload
      }
    jaspr.debug("AFFECT: ", received, performing)
  }

  override def affectService(service: Service): Unit = {
    properties.foreach(p => p._1 match {
      case "MetalPurity" => service.payload = service.payload.asInstanceOf[GoodPayload].copy(quality = p._2.doubleValue)
      case "Rate" => service.duration = Math.round(service.duration - p._2.intValue)
      case "OrePurityReq" => //handled in affectService(Service, Service)
    })
    jaspr.debug("AFFECT: ", service)
  }

  override def makeRequest(assessment: TrustAssessment): Unit = ???

  override def utility: Double = ???

  override def getProvenance[T <: Record]: Seq[T] = ???

  override val memoryLimit: Int = simulation.config.memoryLimit
}
