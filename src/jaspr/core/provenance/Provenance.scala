package jaspr.core.provenance

/**
  * Created by phil on 16/03/16.
  */
trait Provenance {

  val memoryLimit: Int

  def provenanceEmpty = provenance.isEmpty

  protected var provenance: List[Record] = Nil

  def recordProvenance(record: Record): Unit = {
    provenance = record :: provenance.take(memoryLimit - 1)
    jaspr.debug("RECORD: ", this, record, provenance.size)
  }

  def getProvenance[T <: Record](agent: Provenance): Seq[T]

  protected[this] def getProvenance[T <: Record]: Seq[T] = getProvenance[T](this)
}