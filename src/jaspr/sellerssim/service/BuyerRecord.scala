package jaspr.sellerssim.service

import jaspr.core.provenance.{RatingRecord, TrustAssessmentRecord, ServiceRecord, Record}
import jaspr.core.service.{TrustAssessment, Service}

/**
 * Created by phil on 23/03/16.
 */
class BuyerRecord(override val service: Service,
                  override val assessment: TrustAssessment,
                  val ratings: Map[String,Double]
                   ) extends Record with ServiceRecord with TrustAssessmentRecord with RatingRecord {

  override def rating: Double = ratings.values.sum / ratings.size.toDouble
}
