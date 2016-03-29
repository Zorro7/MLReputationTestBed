package jaspr.sellerssim.agent

import jaspr.core.agent.{Property, Provider}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{Payload, ServiceRequest, Service}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.{ProductPayload, SellerService}
import jaspr.utilities.Chooser

/**
 * Created by phil on 21/03/16.
 */
class Seller(override val simulation: SellerSimulation) extends Provider {

  override def receiveRequest(request: ServiceRequest): Boolean = {
    val service = new SellerService(request)
    currentServices += service
    true
  }

  override def affectService(service: Service): Unit = {
    service.payload = capabilities.get(service.payload.name).get.copy()
    if (Chooser.nextDouble() < simulation.config.freakEventLikelihood) {
      jaspr.debug("EVENT:: Storm: ", properties.mapValues(_.doubleValue),
        properties.mapValues(x => (x.doubleValue + simulation.config.freakEventEffects) / 2d))
      service.serviceContext.addEvent(new SellerEvent("Storm"))
      val payloadQuality = service.payload.asInstanceOf[ProductPayload].quality
      service.payload = service.payload.asInstanceOf[ProductPayload].copy(
        quality = payloadQuality.mapValues(x => Chooser.bound((x + simulation.config.freakEventEffects) / 2d, -1d, 1d))
      )
    }
  }

  override def utility: Double = ???

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = ???

  override val memoryLimit: Int = simulation.config.memoryLimit

  override val properties: Map[String, Property] = simulation.config.properties(this)
  override val advertProperties: Map[String, Property] = simulation.config.adverts(this)
  val capabilities: Map[String,ProductPayload] = simulation.config.capabilities(this).map(x => x.name -> x).toMap

  override def capableOf(payload: Payload, duration: Int): Boolean = {
    capabilities.contains(payload.name)
  }

}