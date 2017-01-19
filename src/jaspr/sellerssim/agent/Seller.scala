package jaspr.sellerssim.agent

import jaspr.core.agent.{FixedProperty, Property, Provider}
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
    val payload = capabilities(service.payload.name)
    if (Chooser.nextDouble() < simulation.config.eventLikelihood) {
      jaspr.debug("EVENT:: Storm: ")
      service.serviceContext.addEvent(new SellerEvent("Storm"))
      service.payload = payload.copy(
        quality = payload.quality.map(x => x._1 -> FixedProperty(x._1, Chooser.bound((x._2.doubleValue + simulation.config.eventEffects) / 2d, -1d, 1d)))
      )
    } else {
      service.payload = payload.copy(
        quality = payload.quality.map(x => x._1 -> x._2.sample)
      )
    }
  }

  override def utility: Double = ???

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = Nil

  override val memoryLimit: Int = simulation.config.memoryLimit

  override val properties: SortedMap[String,Property] = simulation.config.properties(this)
  override val adverts: SortedMap[String,Property] = simulation.config.adverts(this)

  val capabilities: Map[String, ProductPayload] = simulation.config.capabilities(this).map(x => x.name -> x).toMap

  val _payloadAdverts: Map[String,SortedMap[String,Property]] = capabilities.values.map(x => x.name -> simulation.config.adverts(x, this)).toMap
  def payloadAdverts(payload: Payload): SortedMap[String,Property] = {
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
