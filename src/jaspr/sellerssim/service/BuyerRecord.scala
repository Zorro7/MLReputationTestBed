package jaspr.sellerssim.service

import jaspr.core.provenance.{TrustAssessmentRecord, ServiceRecord, Record}
import jaspr.core.service.{TrustAssessment, Service}

/**
 * Created by phil on 23/03/16.
 */
class BuyerRecord(override val service: Service,
                  override val assessment: TrustAssessment
                   ) extends Record with ServiceRecord with TrustAssessmentRecord {

}
