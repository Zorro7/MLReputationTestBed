package jaspr.core.provenance

import jaspr.core.agent.{Provider, Client}
import jaspr.core.service.{Service, TrustAssessment}
import jaspr.utilities.NamedEntity

/**
  * Created by phil on 16/03/16.
  */
class Record extends NamedEntity

trait ServiceRecord extends Record {
  val service: Service

  def client: Client = service.request.client
  def provider: Provider = service.request.provider
}

trait TrustAssessmentRecord extends Record {
  val assessment: TrustAssessment
}

trait RatingRecord extends Record {
  def client: Client
  def provider: Provider

  def rating: Double
  def success: Boolean
}