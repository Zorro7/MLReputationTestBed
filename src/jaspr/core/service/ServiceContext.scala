package jaspr.core.service

import jaspr.core.agent.Event
import jaspr.core.provenance.{Provenance, Record}
import jaspr.utilities.NamedEntity

/**
  * Created by phil on 15/03/16.
  */
class ServiceContext extends NamedEntity with Provenance {

  private var _events: List[Event] = Nil

  def events: List[Event] = _events

  def addEvent(event: Event) = {
    _events = event :: _events
  }

  override val memoryLimit: Int = Int.MaxValue

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = provenance.map(_.asInstanceOf[T])
}
