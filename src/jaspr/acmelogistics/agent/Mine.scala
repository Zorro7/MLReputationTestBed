package jaspr.acmelogistics.agent

import jaspr.acmelogistics.ACMESimulation
import jaspr.acmelogistics.service.{ACMEService, GoodPayload}
import jaspr.core.agent.{FixedProperty, Provider}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{Payload, Service, ServiceRequest}

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 17/03/16.
  */
class Mine(val simulation: ACMESimulation) extends Provider {
  override def capableOf(payload: Payload, duration: Int): Boolean = true

  override def receiveRequest(request: ServiceRequest): Boolean = {
    val service = new ACMEService(request)
    jaspr.debug("CREATE: ", request, service)
    currentServices += service
    true
  }

  override def affectService(service: Service): Unit = {
    properties.foreach(p => p._1 match {
      case "OrePurity" => service.payload = service.payload.asInstanceOf[GoodPayload].copy(quality = p._2.doubleValue)
      case "OreWetness" => // nothing?
      case "Rate" => service.duration = Math.round(service.duration - p._2.doubleValue).toInt
    })
    jaspr.debug("AFFECTED: ", service)
  }

  override def utility: Double = ???

  override val properties: SortedMap[String, FixedProperty] = simulation.config.properties(this)
  override val generalAdverts: SortedMap[String, FixedProperty] = simulation.config.adverts(this)
  override def payloadAdverts(payload: Payload): SortedMap[String, FixedProperty] = generalAdverts

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = provenance.map(_.asInstanceOf[T])

  override val memoryLimit: Int = simulation.config.memoryLimit
}
