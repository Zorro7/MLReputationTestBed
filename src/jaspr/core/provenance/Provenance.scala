package jaspr.core.provenance

import jaspr.core.Simulation

/**
 * Created by phil on 16/03/16.
 */
trait Provenance {

  val simulation: Simulation
  val memoryLimit: Int

  protected var provenance: List[Record] = Nil

  def recordProvenance(record: Record): Unit = {
    provenance = record :: provenance.take(memoryLimit - 1)
    jaspr.debug("RECORD: ", this, record, provenance.size)
  }

  def getProvenance[T <: Record]: Seq[T]
  def gatherProvenance[T <: Record](): Seq[T]

}