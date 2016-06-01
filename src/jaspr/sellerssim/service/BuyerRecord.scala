package jaspr.sellerssim.service

import jaspr.core.agent.{Client, Provider, Event}
import jaspr.core.provenance.{RatingRecord, TrustAssessmentRecord, ServiceRecord, Record}
import jaspr.core.service.{Payload, TrustAssessment, Service}

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
                  ratings: Map[String,Double]
                   ) extends Record with ServiceRecord with TrustAssessmentRecord with RatingRecord {

  override def rating: Double = {
    println(ratings, ratings.values.sum / ratings.size.toDouble)
    ratings.values.sum / ratings.size.toDouble
  }

  override def toString: String = List(
    client, provider, round, payload, event, ratings, rating
  ).mkString(",")
}
