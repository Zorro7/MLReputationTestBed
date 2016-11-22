package jaspr.simplesim.provenance

import jaspr.core.provenance.{RatingRecord, ServiceRecord}
import jaspr.core.service.Service

/**
  * Created by phil on 16/03/16.
  */
class SimpleRecord(override val service: Service) extends ServiceRecord with RatingRecord {

  def rating: Double = service.utility()
  def success: Boolean = rating > 0
}
