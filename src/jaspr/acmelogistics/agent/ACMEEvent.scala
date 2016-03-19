package jaspr.acmelogistics.agent

import jaspr.core.agent.{Provider, Event}
import jaspr.core.service.Service

/**
 * Created by phil on 19/03/16.
 */
class ACMEEvent(override val providers: Seq[Provider]) extends Event {

  override def affect(service: Service): Unit = {
    service.duration = service.duration + 1
  }

  override def affect(provider: Provider): Unit = {}
}
