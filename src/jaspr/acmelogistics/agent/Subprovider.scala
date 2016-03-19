package jaspr.acmelogistics.agent

import jaspr.acmelogistics.service.{SubproviderRecord, ACMERecord, ACMEService}
import jaspr.core.agent.{Provider, Client}
import jaspr.core.provenance.ServiceRecord
import jaspr.core.service.{Service, ServiceRequest, TrustAssessment, ClientContext}

import scala.collection.mutable

/**
 * Created by phil on 17/03/16.
 */
abstract class Subprovider extends Client with Provider {

  override def tick(): Unit = {
    super[Provider].tick()

  }

  def generateContext(): ClientContext = ???
  def generateComposition(context: ClientContext): TrustAssessment = ???

  def receiveRequest(request: ServiceRequest): Boolean = {
    val service = new ACMEService(request)
    for (deprequest <- request.dependencies) {
      deprequest.provider.receiveRequest(deprequest)
      currentRequests.put(deprequest, service)
    }
    jaspr.debug("CREATE: ", request, service)
    currentServices += service
    true
  }

  val currentRequests: mutable.Map[ServiceRequest, Service] = new mutable.HashMap()

  override def receiveService(service: Service): Unit = {
    jaspr.debug("RECEIVE: ", this, service)
    val record = new SubproviderRecord(service)
    recordProvenance(record)
    currentRequests.remove(service.request) match {
      case Some(x) =>
        x.serviceContext.recordProvenance(record)
        affectService(x, service)
        tryStartService(x)
      case None =>
        throw new Exception("Service "+service+" not found.")
    }
  }

  def affectService(performing: Service, received: Service): Unit

  override val memoryLimit: Int = _

}
