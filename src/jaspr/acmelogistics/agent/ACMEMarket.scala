package jaspr.acmelogistics.agent

import jaspr.acmelogistics.service.GoodPayload
import jaspr.core.Simulation
import jaspr.core.agent.Market
import jaspr.core.service.Service

/**
 * Created by phil on 17/03/16.
 */
class ACMEMarket(val simulation: Simulation) extends Market {

  override def deliver(service: Service): Double = {
    def penalizeTime(agreedCompletion: Int, completion: Int, agreedStart: Int): Double = {
      Math.max(0d, (completion - agreedCompletion).toDouble / (agreedCompletion - agreedStart).toDouble)
    }
    def penalizeGood(request: GoodPayload, delivered: GoodPayload) = {
      Math.abs(request.quality*request.quantity - delivered.quality*delivered.quantity)
    }
    val requestPayload = service.request.payload.asInstanceOf[GoodPayload]
    val deliveredPayload = service.payload.asInstanceOf[GoodPayload]

    val deliveredValue = deliveredPayload.quality*deliveredPayload.quantity
    val requestValue = requestPayload.quality*requestPayload.quantity
    val fines: Double =
      penalizeTime(service.request.end, service.end, service.request.flatten().last.start) +
        penalizeGood(requestPayload, deliveredPayload)
    deliveredValue - deliveredValue*fines
  }

}
