package jaspr.sellerssim.agent

import jaspr.core.agent.{Property, Provider}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{Payload, ServiceRequest, Service}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.SellerService

/**
 * Created by phil on 21/03/16.
 */
class Seller(override val simulation: SellerSimulation) extends Provider {

  val capabilities: Seq[Payload] = new Payload :: Nil

  override def capableOf(payload: Payload, duration: Int): Boolean = true

  override def receiveRequest(request: ServiceRequest): Boolean = {
    val service = new SellerService(request)
    currentServices += service
    true
  }

  override def affectService(service: Service): Unit = {}

  override def utility: Double = ???

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = ???

  override val memoryLimit: Int = simulation.config.memoryLimit

  override def advertProperties: Map[String, Property] = ???

  override def properties: Map[String, Property] = ???
}
