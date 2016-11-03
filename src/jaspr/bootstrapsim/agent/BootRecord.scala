package jaspr.bootstrapsim.agent

import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.{Service, ServiceRequest, TrustAssessment}

/**
  * Created by phil on 29/09/2016.
  */
class BootRecord(override val assessment: TrustAssessment,
                 override val service: Service
                ) extends Record with TrustAssessmentRecord with ServiceRecord with RatingRecord {

  val truster: Truster = service.request.client.asInstanceOf[Truster]
  val trustee: Trustee = service.request.provider.asInstanceOf[Trustee]

  override def rating: Double = service.utility()
  def success: Boolean = rating > 0.5

  val observations: Map[Trustee,List[Any]] = {
    assessment match {
      case x: Observations =>
        (service.request :: x.possibleRequests.toList).map(x =>
          x.provider.asInstanceOf[Trustee] -> x.properties.values.map(_.value.toString).toList
        ).toMap
      case _ => Map(trustee -> service.request.properties.values.map(_.value.toString).toList)
    }
  }
}

trait Observations {
  val possibleRequests: Seq[ServiceRequest]
}