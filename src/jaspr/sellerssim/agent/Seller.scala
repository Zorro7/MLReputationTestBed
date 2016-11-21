package jaspr.sellerssim.agent

import jaspr.core.agent.{FixedProperty, Provider}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{Payload, Service, ServiceRequest}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.{ProductPayload, SellerService}
import jaspr.utilities.Chooser

import scala.collection.immutable.SortedMap

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
    service.payload = capabilities(service.payload.name).copy()
    if (Chooser.nextDouble() < simulation.config.eventLikelihood) {
      jaspr.debug("EVENT:: Storm: ", properties.mapValues(_.doubleValue),
        properties.mapValues(x => (x.doubleValue + simulation.config.eventEffects) / 2d))
      service.serviceContext.addEvent(new SellerEvent("Storm"))
      val payloadQuality = service.payload.asInstanceOf[ProductPayload].quality
      service.payload = service.payload.asInstanceOf[ProductPayload].copy(
        quality = payloadQuality.mapValues(x => FixedProperty(x.name, Chooser.bound((x.doubleValue + simulation.config.eventEffects) / 2d, -1d, 1d)))
      )
    }
  }

  override def utility: Double = ???

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = Nil

  override val memoryLimit: Int = simulation.config.memoryLimit

  override val properties: SortedMap[String, FixedProperty] = simulation.config.properties(this)
  override val generalAdverts: SortedMap[String, FixedProperty] = simulation.config.adverts(this)

  val capabilities: Map[String, ProductPayload] = simulation.config.capabilities(this).map(x => x.name -> x).toMap

  val _payloadAdverts: Map[String,SortedMap[String,FixedProperty]] = capabilities.values.map(x => x.name -> simulation.config.adverts(x, this)).toMap
  override def payloadAdverts(payload: Payload): SortedMap[String,FixedProperty] = {
    _payloadAdverts(payload.name)
  }

  override def capableOf(payload: Payload, duration: Int): Boolean = {
    payload match {
      case pp: ProductPayload =>
        capabilities.contains(payload.name) &&
          pp.quality.forall(x => capabilities(payload.name).quality.contains(x._1))
      case _ => capabilities.contains(payload.name)
    }
  }
}
