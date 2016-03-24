package jaspr.sellerssim.agent

import jaspr.core.agent.{Property, Provider}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{Payload, ServiceRequest, Service}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.{ProductPayload, SellerService}

/**
 * Created by phil on 21/03/16.
 */
class Seller(override val simulation: SellerSimulation) extends Provider {

  val capabilities: Seq[Payload] = simulation.config.capabilities(this)

  override def capableOf(payload: Payload, duration: Int): Boolean = {
    capabilities.exists(x => x.name == payload.name)
  }

  override def receiveRequest(request: ServiceRequest): Boolean = {
    val service = new SellerService(request)
    currentServices += service
    true
  }

  override def affectService(service: Service): Unit = {
    service.payload.asInstanceOf[ProductPayload].quality = properties.mapValues(_.doubleValue)
  }

  override def utility: Double = ???

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = ???

  override val memoryLimit: Int = simulation.config.memoryLimit

  override val properties: Map[String, Property] = simulation.config.properties(this)
  override val advertProperties: Map[String, Property] = simulation.config.adverts(this)

}
