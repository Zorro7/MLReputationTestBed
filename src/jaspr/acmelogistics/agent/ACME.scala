package jaspr.acmelogistics.agent

import jaspr.acmelogistics.ACMESimulation
import jaspr.acmelogistics.service.ACMERecord
import jaspr.acmelogistics.strategy.ipaw.{IpawEvents, Ipaw}
import jaspr.core.agent.Client
import jaspr.core.provenance.{Provenance, ServiceRecord, Record}
import jaspr.core.service.{ServiceRequest, Service, TrustAssessment, ClientContext}
import weka.classifiers.functions.LinearRegression

import scala.collection.mutable

/**
 * Created by phil on 17/03/16.
 */
class ACME(override val simulation: ACMESimulation) extends Client {

  override def generateContext(): ClientContext = {
    val context = simulation.config.clientContext(simulation.network, this, simulation.round)
    jaspr.debug("generateContext: ", this, context)
    context
  }

  override def receiveService(service: Service): Unit = {
    jaspr.debug("RECEIVE: ", service)
    val assessment: TrustAssessment = trustAssessments.remove(service.request) match {
      case Some(x) =>
        val gain = service.utility()
        jaspr.debug(10, "UTILITY: ", simulation.round, this, utility, gain)
        utility += gain
        x
      case None => throw new Exception("Request "+service.request+" not found.")
    }
    recordProvenance(new ACMERecord(service, assessment))
  }

  override def makeRequest(assessment: TrustAssessment): Unit = {
    assessment.request.provider.receiveRequest(assessment.request)
  }

  override def generateComposition(context: ClientContext): TrustAssessment = {
//    val ipaw = new Ipaw(new LinearRegression, false)
//    val ipawEvents = new IpawEvents(new LinearRegression, false)
//
//    val requests = simulation.network.possibleRequests(context)
//    val ipawInit = ipaw.initStrategy(simulation.network, context)
//    val eventsInit = ipawEvents.initStrategy(simulation.network, context)
//    val ipawAss = ipaw.rank(ipawInit, requests).sortBy(_.request.toString)
//    val eventsAss = ipawEvents.rank(eventsInit, requests).sortBy(_.request.toString)

//    println(context.payload)
//    for ((a,b) <- ipawAss zip eventsAss) {
//      println(a+"\n\t"+b)
//    }
//    println()

    config.strategy.assessReputation(simulation.network, context)
  }

  var utility: Double = 0d

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = provenance.map(_.asInstanceOf[T])

  override val memoryLimit: Int = simulation.config.memoryLimit
}
