package jaspr.marketsim.agent

import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.{Service, TrustAssessment}

/**
  * Created by phil on 18/01/17.
  */
class MarketRecord(override val assessment: TrustAssessment,
                   override val service: Service
                  ) extends Record with TrustAssessmentRecord with ServiceRecord with RatingRecord {

  override def rating: Double = service.utility()

  def success: Boolean = rating > 0.5
}