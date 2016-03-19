package jaspr.core.provenance

import jaspr.core.agent.Agent

/**
 * Created by phil on 16/03/16.
 */
trait Provenance {

  val memoryLimit: Int

  protected var provenance: List[Record] = Nil

  def recordProvenance(record: Record): Unit = {
    provenance = record :: provenance.take(memoryLimit - 1)
    jaspr.debug("RECORD: ", this, record, provenance.size)
  }

  def getProvenance[T <: Record](agent: Agent): Seq[T] = getProvenance[T]
  protected def getProvenance[T <: Record]: Seq[T]
}