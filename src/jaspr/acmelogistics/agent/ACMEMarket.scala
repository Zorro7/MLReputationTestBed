package jaspr.acmelogistics.agent

import jaspr.core.Simulation
import jaspr.core.agent.Market
import jaspr.core.service.Service

/**
 * Created by phil on 17/03/16.
 */
class ACMEMarket(val simulation: Simulation) extends Market {

  override def deliver(service: Service): Double = {
//    def penalizeTime(agreedCompletion: Int, completion: Int, agreedStart: Int): Double = {
//      Math.max(0d, (completion - agreedCompletion).toDouble / (agreedCompletion - agreedStart).toDouble)
//    }
//    def penalizeGood(agreedValue: Double, receivedValue: Double) = {
//      Math.abs(agreedValue - receivedValue)
//    }
//    val agreedValue: Double =
//      service.request.properties.getOrElse("GoodQuality", null).doubleValue * service.request.properties.getOrElse("GoodQuantity", null).doubleValue
//    val receivedValue: Double =
//      service.properties.getOrElse("GoodQuality", null).doubleValue * service.properties.getOrElse("GoodQuantity", null).doubleValue
//    val fines: Double =
//      penalizeTime(service.request.end, service.end, service.request.start) +
//        penalizeGood(agreedValue, receivedValue)
//    receivedValue - receivedValue*fines
    0d
  }

}
