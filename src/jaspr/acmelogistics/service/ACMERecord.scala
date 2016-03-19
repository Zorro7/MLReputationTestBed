package jaspr.acmelogistics.service

import jaspr.core.provenance.{RatingRecord, TrustAssessmentRecord, ServiceRecord}
import jaspr.core.service.{TrustAssessment, Service}

/**
 * Created by phil on 17/03/16.
 */
class ACMERecord(override val service: Service,
                 override val assessment: TrustAssessment
                  ) extends ServiceRecord with TrustAssessmentRecord with RatingRecord {
  def rating: Double = service.utility()
}

class SubproviderRecord(override val service: Service) extends ServiceRecord with RatingRecord {
  def rating: Double = service.utility()
}