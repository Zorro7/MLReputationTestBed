package jaspr.core.service

import jaspr.core.agent.Event
import jaspr.utilities.NamedEntity

/**
 * Created by phil on 15/03/16.
 */
class ServiceContext extends NamedEntity {

  private var _events: List[Event] = Nil
  def events: List[Event] = _events
  def addEvent(event: Event) = {
    _events = event :: _events
  }
}
