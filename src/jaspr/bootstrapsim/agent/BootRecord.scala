package jaspr.bootstrapsim.agent

import jaspr.core.provenance.{RatingRecord, TrustAssessmentRecord, ServiceRecord, Record}
import jaspr.core.service.{TrustAssessment, Service}

/**
  * Created by phil on 29/09/2016.
  */
class BootRecord(override val assessment: TrustAssessment,
                 override val service: Service
                ) extends Record with TrustAssessmentRecord with ServiceRecord with RatingRecord {

  override def rating: Double = service.utility()
  def success: Boolean = rating > 0.5
}
