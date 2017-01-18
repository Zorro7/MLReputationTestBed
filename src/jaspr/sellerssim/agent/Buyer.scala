package jaspr.sellerssim.agent

import jaspr.core.agent.{Client, Preferences}
import jaspr.core.service.{ClientContext, Service, TrustAssessment}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.{BuyerRecord, ProductPayload}
import jaspr.utilities.Chooser

/**
  * Created by phil on 21/03/16.
  */
class Buyer(override val simulation: SellerSimulation) extends Client with Preferences with Witness {

  override def generateContext(): ClientContext = {
    var context = simulation.config.clientContext(simulation.network, this, simulation.round)
    while (!simulation.network.providers.exists(_.capableOf(context.payload, 0))) {
      context = simulation.config.clientContext(simulation.network, this, simulation.round)
    }
    context
  }

  override def receiveService(service: Service): Unit = {
    jaspr.debug("RECEIVE: ", service)
    val assessment: TrustAssessment = trustAssessments.remove(service.request) match {
      case Some(x) =>
        val gain = service.utility()
        jaspr.debug(10, "UTILITY: ", simulation.round, this, utility, gain)
        _utility += gain
//        println(gain)
        x
      case None => throw new Exception("Request " + service.request + " not found.")
    }
    recordProvenance(BuyerRecord(service, assessment, rateService(service)))
  }


  def rateService(service: Service): Map[String, Double] = {
    val received = service.payload.asInstanceOf[ProductPayload].quality
    val wanted = service.request.payload.asInstanceOf[ProductPayload].quality
    received.withFilter(x => wanted.contains(x._1)).map(x =>
      x._1 -> {
        simulation.config.baseUtility - Math.abs(x._2.doubleValue - wanted(x._1).doubleValue)
//        bound(simulation.config.baseUtility - Math.abs(x._2.doubleValue - wanted(x._1).doubleValue),0,1)
//        wanted(x._1).doubleValue
//        x._2.doubleValue
//        simulation.config.baseUtility - (wanted(x._1).doubleValue - x._2.doubleValue)
//        simulation.config.baseUtility * (x._2.doubleValue - wanted(x._1).doubleValue)
//        simulation.config.baseUtility - (x._2.doubleValue - wanted(x._1).doubleValue)
//        val ut = if (Math.abs(x._2.doubleValue - wanted(x._1).doubleValue) > 0.5) {
//          x._2.doubleValue
//        } else {
//          bound(simulation.config.baseUtility - (wanted(x._1).doubleValue - x._2.doubleValue), 0, 1)
//          0d
//        }
//        println(simulation.config.baseUtility, x._2.doubleValue, wanted(x._1).doubleValue, simulation.config.baseUtility - (x._2.doubleValue - wanted(x._1).doubleValue), ut)
//        ut
      }
    )
  }

  def bound(x: Double, mn: Double, mx: Double): Double = {
    Math.min(Math.max(x,mn),mx)
  }

  override def makeRequest(assessment: TrustAssessment): Unit = {
    jaspr.debug("REQUEST: ", assessment.request)
    assessment.request.provider.receiveRequest(assessment.request)
  }

  override def generateComposition(context: ClientContext): TrustAssessment = {
    simulation.config.strategy(this).assessReputation(simulation.network, context)
  }

  private var _utility: Double = 0d

  override def utility: Double = _utility

  override val memoryLimit: Int = simulation.config.memoryLimit

  override val preferences = simulation.config.preferences(this)
}

