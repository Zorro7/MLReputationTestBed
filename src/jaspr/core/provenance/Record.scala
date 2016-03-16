package jaspr.core.provenance

import jaspr.core.agent.Properties
import jaspr.core.service.{Service, TrustAssessment}
import jaspr.utilities.NamedEntity

/**
 * Created by phil on 16/03/16.
 */
class Record extends NamedEntity

trait ServiceRecord extends Record {
  val service: Service
}

trait TrustAssessmentRecord extends Record {
  val trustAssessment: TrustAssessment
}

trait RatingsRecord extends Record with Properties