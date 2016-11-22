package jaspr.sellerssim.service

import jaspr.core.agent.{Client, Event, Provider}
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.{Payload, Service, TrustAssessment}
import jaspr.sellerssim.agent.SellerEvent

/**
  * Created by phil on 23/03/16.
  */

case class BuyerRecord(override val service: Service,
                       override val assessment: TrustAssessment,
                       ratings: Map[String, Double]
                      ) extends Record with ServiceRecord with TrustAssessmentRecord with RatingRecord {

  override def rating: Double = ratings.values.sum / ratings.size.toDouble

  def round: Int = service.end

  def payload: Payload = service.payload

  def event: Event = {
    service.serviceContext.events.headOption match {
      case Some(x) => x
      case None => new SellerEvent("NA")
    }
  }

  override def toString: String = List(
    ratings, rating
  ).mkString(",")
}
