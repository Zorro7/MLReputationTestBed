package jaspr.marketsim.agent

import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.{Service, TrustAssessment}

/**
  * Created by phil on 18/01/17.
  */
class MarketRecord(override val assessment: TrustAssessment,
                   override val service: Service
                  ) extends Record with TrustAssessmentRecord with ServiceRecord with RatingRecord {

  val delivered = service.payload.asInstanceOf[MarketPayload]

  override val rating: Double = {
    val requested = service.request.payload.asInstanceOf[MarketPayload]
//    println(delivered.quality, service.request.provider.properties, requested.quality)
    val disparity = requested.properties.map(r =>
      delivered.properties.get(r._1) match {
        case Some(d) => d.doubleValue - r._2.doubleValue
        case None => 0
      }
    )
    disparity.sum / disparity.size.toDouble
  }

  def success: Boolean = rating > 0
}