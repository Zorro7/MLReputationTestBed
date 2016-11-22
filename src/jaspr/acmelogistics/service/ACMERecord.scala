package jaspr.acmelogistics.service

import jaspr.core.provenance.{RatingRecord, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.{Service, TrustAssessment}

/**
  * Created by phil on 17/03/16.
  */
class ACMERecord(override val service: Service,
                 override val assessment: TrustAssessment
                ) extends ServiceRecord with TrustAssessmentRecord with RatingRecord {
  def rating: Double = service.utility()
  def success: Boolean = rating > 0
}

class SubproviderRecord(override val service: Service) extends ServiceRecord with RatingRecord {
  def rating: Double = service.utility()
  def success: Boolean = rating > 0
}