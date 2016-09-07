package jaspr.core.agent

import jaspr.core.service.{ClientContext, Service, ServiceRequest, TrustAssessment}

import scala.collection.mutable

/**
  * Created by phil on 15/03/16.
  */
trait Client extends Agent {

  def tick(): Unit = {
    jaspr.debug("TICK (Client): ", this)
    val context = generateContext()
    val assessment = generateComposition(context)
    trustAssessments.put(assessment.request, assessment)
    makeRequest(assessment)
  }

  val trustAssessments: mutable.Map[ServiceRequest, TrustAssessment] = new mutable.HashMap

  def generateContext(): ClientContext

  def generateComposition(context: ClientContext): TrustAssessment

  def makeRequest(assessment: TrustAssessment): Unit

  def receiveService(service: Service): Unit
}
