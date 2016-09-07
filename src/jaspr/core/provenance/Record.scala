package jaspr.core.provenance

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
  val assessment: TrustAssessment
}

trait RatingRecord extends Record {
  def rating: Double
}