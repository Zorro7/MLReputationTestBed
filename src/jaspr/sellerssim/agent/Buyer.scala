package jaspr.sellerssim.agent

import jaspr.core.agent.{Client, Preferences}
import jaspr.core.service.{ClientContext, Service, TrustAssessment}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.{BuyerRecord, ProductPayload}

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
        x
      case None => throw new Exception("Request " + service.request + " not found.")
    }
    recordProvenance(new BuyerRecord(service, assessment, rateService(service)))
  }


  def rateService(service: Service): Map[String, Double] = {
    val received = service.payload.asInstanceOf[ProductPayload].quality
    val wanted = service.request.payload.asInstanceOf[ProductPayload].quality
    //    received.map(x => x._1 -> {
    //      wanted.get(x._1) match {
    //        case Some(req) => simulation.config.baseUtility - Math.abs(x._2 - req)
    //        case None => simulation.config.baseUtility - Math.abs(x._2)
    //      }
    //    })
    val x = received.withFilter(x => wanted.contains(x._1)).map(x =>
      x._1 -> (simulation.config.baseUtility - Math.abs(x._2 - wanted(x._1)))
    )
//    println(x.values.sum/x.size, wanted.values, received.values, x)
    x
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

