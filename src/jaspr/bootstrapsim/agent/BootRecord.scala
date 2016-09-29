package jaspr.bootstrapsim.agent

import jaspr.core.agent.{Property, Properties}
import jaspr.core.provenance.{TrustAssessmentRecord, ServiceRecord, Record}
import jaspr.core.service.{TrustAssessment, Service}

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 29/09/2016.
  */
class BootRecord(override val assessment: TrustAssessment,
                 override val service: Service
                ) extends Record with TrustAssessmentRecord with ServiceRecord {
}
