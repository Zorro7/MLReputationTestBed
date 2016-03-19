package jaspr.simplesim.provenance

import jaspr.core.provenance.{ServiceRecord, RatingRecord}
import jaspr.core.service.Service

/**
 * Created by phil on 16/03/16.
 */
class SimpleRecord(override val service: Service) extends ServiceRecord with RatingRecord {

  def rating: Double = service.utility()
}
