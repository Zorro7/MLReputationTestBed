package jaspr.sellerssim.agent

import jaspr.core.agent.{Event, Agent, Client}
import jaspr.core.provenance.{RatingRecord, Provenance, Record}
import jaspr.core.service.{ServiceRequest, Service, TrustAssessment, ClientContext}
import jaspr.core.strategy.StrategyInit
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.{ProductPayload, BuyerRecord}

import scala.collection.mutable

/**
 * Created by phil on 21/03/16.
 */
class Buyer(override val simulation: SellerSimulation) extends Client with Witness {

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
//    val si: Double = mlrsAUCs.remove(service.request).get
//    val dr: Double = mlrsDRs.remove(service.request).get
//    val wr: Double = mlrsWRs.remove(service.request).get
//    println(si, dr, wr, service.utility(), simulation.network.utility())
  }

  val mlrsAUCs: mutable.Map[ServiceRequest, Double] = new mutable.HashMap
  val mlrsDRs: mutable.Map[ServiceRequest, Int] = new mutable.HashMap
  val mlrsWRs: mutable.Map[ServiceRequest, Int] = new mutable.HashMap

  var tmp: List[Double] = Nil

  def rateService(service: Service): Map[String,Double] = {
    val received = service.payload.asInstanceOf[ProductPayload].quality
    val wanted = service.request.payload.asInstanceOf[ProductPayload].quality
    received.map(x => x._1 -> {
      wanted.get(x._1) match {
        case Some(req) => simulation.config.baseUtility - Math.abs(x._2 - req)
        case None => simulation.config.baseUtility + x._2
      }
    })
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

  override val memoryLimit: Int = simulation.config.memoryLimit

  override val preferences = simulation.config.preferences(this)
}

