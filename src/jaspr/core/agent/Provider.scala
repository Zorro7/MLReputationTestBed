package jaspr.core.agent

import jaspr.core.service.{Payload, Service, ServiceRequest}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by phil on 15/03/16.
  */
trait Provider extends Agent with AdvertProperties {

  val currentServices: mutable.ListBuffer[Service] = new ListBuffer[Service]

  def tick(): Unit = {
    jaspr.debug("TICK (Provider): ", this)
    tryStartServices()
    tryDeliverServices()
  }

  def tryStartServices(): Unit = {
    for (service <- currentServices) {
      tryStartService(service)
    }
  }

  def tryStartService(service: Service): Unit = {
    if (service.tryStartService(simulation.round)) {
      affectService(service)
    }
  }

  def tryDeliverServices(): Unit = {
    for (service <- currentServices) {
      if (service.tryEndService(simulation.round)) {
        currentServices -= service
        service.request.client.receiveService(service)
      }
    }
  }

  def capableOf(payload: Payload, duration: Int): Boolean

  def receiveRequest(request: ServiceRequest): Boolean

  def affectService(service: Service): Unit
}
