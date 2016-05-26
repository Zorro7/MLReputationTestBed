package jaspr.sellerssim.agent

import jaspr.core.agent.{Event, Agent, Client}
import jaspr.core.provenance.{RatingRecord, Provenance, Record}
import jaspr.core.service.{Service, TrustAssessment, ClientContext}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.{ProductPayload, BuyerRecord}

/**
 * Created by phil on 21/03/16.
 */
class Buyer(override val simulation: SellerSimulation) extends Client {

  override def generateContext(): ClientContext = {
    simulation.config.clientContext(simulation.network, this, simulation.round)
  }

  override def receiveService(service: Service): Unit = {
    jaspr.debug("RECEIVE: ", service)
    val assessment: TrustAssessment = trustAssessments.remove(service.request) match {
      case Some(x) =>
        val gain = service.utility()
        jaspr.debug(10, "UTILITY: ", simulation.round, this, utility, gain)
        _utility += gain
        x
      case None => throw new Exception("Request "+service.request+" not found.")
    }
    recordProvenance(new BuyerRecord(
      service, assessment,
      service.request.client, service.request.provider,
      service.end, service.payload, service.serviceContext.events.headOption match {
        case Some(x) => x
        case None => new SellerEvent("NA")
      },
      rateService(service)
    ))
  }

  def sigmoid(x: Double) = 1d/(1d+Math.exp(Math.abs(-x)))

  def rateService(service: Service): Map[String,Double] = {
    val received = service.payload.asInstanceOf[ProductPayload].quality
    val wanted = service.request.payload.asInstanceOf[ProductPayload].quality
    val x = received.map(x => x._1 -> {
      val req = wanted.getOrElse(x._1, 0d)
//      sigmoid(1d / Math.abs((x._2-req)+1d))
      1d - Math.abs(x._2 - req)
    })
    println(wanted)
    println(received)
    println("\t"+x)
    x
  }

  override def makeRequest(assessment: TrustAssessment): Unit = {
    jaspr.debug("REQUEST: ", assessment.request)
    assessment.request.provider.receiveRequest(assessment.request)
  }

  override def generateComposition(context: ClientContext): TrustAssessment = {
    simulation.config.strategy.assessReputation(simulation.network, context)
  }

  private var _utility: Double = 0d
  override def utility: Double = _utility

  def changeRatings: Map[String,Double] => Map[String,Double] = simulation.config.changeRatings(this)

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = {
    def omitRecord(record: BuyerRecord, agent: Provenance): Boolean = {
      false
    }
    if (agent == this) {
      provenance.map(_.asInstanceOf[T])
    } else {
      provenance.withFilter(
        x => !omitRecord(x.asInstanceOf[BuyerRecord], agent)
      ).map(x =>
        x.asInstanceOf[BuyerRecord].copy(ratings = changeRatings(x.asInstanceOf[BuyerRecord].ratings)).asInstanceOf[T]
      )
    }
  }

  override val memoryLimit: Int = simulation.config.memoryLimit

  override val preferences = simulation.config.properties(this)
}
