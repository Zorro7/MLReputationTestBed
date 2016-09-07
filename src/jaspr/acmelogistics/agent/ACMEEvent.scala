package jaspr.acmelogistics.agent

import jaspr.core.agent.{Event, Provider}
import jaspr.core.service.Service

/**
  * Created by phil on 19/03/16.
  */
class ACMEEvent(override val providers: Seq[Provider], val delay: Int) extends Event {

  override def affect(service: Service): Unit = {
    if (service.serviceContext.events.isEmpty) service.duration = service.duration + delay
    //    val payload = service.payload.asInstanceOf[GoodPayload]
    //    service.payload = payload.copy(quantity = 0d)
  }

  override def affect(provider: Provider): Unit = {}
}
