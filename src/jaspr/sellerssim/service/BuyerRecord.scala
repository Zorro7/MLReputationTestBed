package jaspr.sellerssim.service

import jaspr.core.agent.{Client, Event, Provider}
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.{Payload, Service, TrustAssessment}

/**
  * Created by phil on 23/03/16.
  */

case class BuyerRecord(override val service: Service,
                       override val assessment: TrustAssessment,
                       client: Client,
                       provider: Provider,
                       round: Int,
                       payload: Payload,
                       event: Event,
                       ratings: Map[String, Double]
                      ) extends Record with ServiceRecord with TrustAssessmentRecord with RatingRecord {

  override def rating: Double = ratings.values.sum / ratings.size.toDouble

  override def toString: String = List(
    client, provider, round, payload, event, ratings, rating
  ).mkString(",")
}
