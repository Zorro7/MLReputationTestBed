package jaspr.acmelogistics.agent

import jaspr.acmelogistics.ACMESimulation
import jaspr.acmelogistics.service.{GoodPayload, ACMEService}
import jaspr.core.agent.{Property, Provider}
import jaspr.core.provenance.Record
import jaspr.core.service.{ServiceRequest, Service}

/**
 * Created by phil on 17/03/16.
 */
class Mine(val simulation: ACMESimulation) extends Provider {
  override def receiveRequest(request: ServiceRequest): Boolean = {
    val service = new ACMEService(request)
    jaspr.debug("CREATE: ", request, service)
    currentServices += service
    true
  }

  override def affectService(service: Service): Unit = {}

  override def utility: Double = ???

  override val properties: Map[String, Property] = simulation.config.properties(this)
  override val advertProperties: Map[String, Property] = simulation.config.adverts(this)

  override def gatherProvenance[T <: Record](): Seq[T] = ???

  override def getProvenance[T <: Record]: Seq[T] = ???

  override val memoryLimit: Int = simulation.config.memoryLimit
}
