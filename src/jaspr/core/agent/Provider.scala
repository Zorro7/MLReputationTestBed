package jaspr.core.agent

import jaspr.core.service.{ServiceRequest, Service}

import scala.collection.mutable

/**
 * Created by phil on 15/03/16.
 */
trait Provider extends Agent {

  val currentServices: mutable.ListBuffer[Service]

  def tick(): Unit = {
    jaspr.debug("TICK (Provider): ", this)
    tryStartServices()
    tryDeliverServices()
  }

  def tryStartServices(): Unit  = {
    for (service <- currentServices) {
      if (service.tryStartService(simulation.round)) {
        affectService(service)
      }
    }
  }

  def tryDeliverServices(): Unit = {
    for (service <- currentServices) {
      if (service.tryEndService(simulation.round)) {
        service.request.client.receiveService(service)
      }
    }
  }

  def receiveRequest(request: ServiceRequest): Boolean
  def affectService(service: Service): Unit
}
