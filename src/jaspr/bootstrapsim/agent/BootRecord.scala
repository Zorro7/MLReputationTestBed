package jaspr.bootstrapsim.agent

import jaspr.core.agent.{Property, Properties}
import jaspr.core.provenance.{ServiceRecord, Record}
import jaspr.core.service.Service

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 29/09/2016.
  */
class BootRecord(override val service: Service) extends Record with ServiceRecord {
}
