package jaspr.simplesim.provenance

import jaspr.core.agent.Property
import jaspr.core.provenance.{RatingsRecord, ServiceRecord}
import jaspr.core.service.Service


/**
 * Created by phil on 16/03/16.
 */
class SimpleRecord(override val service: Service,
                   override val properties: Map[String,Property]
                    ) extends ServiceRecord with RatingsRecord
