package jaspr.acmelogistics.agent

import jaspr.acmelogistics.ACMESimulation
import jaspr.acmelogistics.service.GoodPayload
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{Service, TrustAssessment}

/**
  * Created by phil on 17/03/16.
  */
class Shipper(simulation: ACMESimulation) extends Subprovider(simulation) {
  override def affectService(service: Service): Unit = {
    properties.foreach(p => p._1 match {
      case "Competence" =>
        service.payload = service.payload.asInstanceOf[GoodPayload].copy(
          quantity = service.payload.asInstanceOf[GoodPayload].quantity + p._2.doubleValue
        )
      case "Timeliness" => service.duration = Math.round(service.duration - p._2.doubleValue).toInt
      case "Capacity" => //performing.good = performing.good.copy(quantity = performing.good.quantity + p._2)
    })
    jaspr.debug("AFFECT: ", service)
  }

  override def affectService(performing: Service, received: Service): Unit = {
    performing.payload = received.payload
    jaspr.debug("AFFECT: ", received, performing)
  }

  override def makeRequest(assessment: TrustAssessment): Unit = ???

  override def utility: Double = ???

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = provenance.map(_.asInstanceOf[T])

  override val memoryLimit: Int = simulation.config.memoryLimit
}
