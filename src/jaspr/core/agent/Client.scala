package jaspr.core.agent

import jaspr.core.service.{Service, TrustAssessment, ClientContext}

/**
 * Created by phil on 15/03/16.
 */
trait Client extends Agent {

  def tick(): Unit = {
    jaspr.debug("TICK (Client): ", this)
    val context = generateContext()
    val assessment = generateComposition(context)
    makeRequest(assessment)
  }

  def generateContext(): ClientContext
  def generateComposition(context: ClientContext): TrustAssessment
  def makeRequest(assessment: TrustAssessment): Unit
  def receiveService(service: Service): Unit
}
