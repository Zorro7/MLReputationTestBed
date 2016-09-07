package jaspr.simplesim.agent

import jaspr.core.agent.{Event, Provider}
import jaspr.core.service.Service


/**
  * Created by phil on 17/03/16.
  */
case class SimpleEvent(override val name: String, providers: Seq[Provider]) extends Event {

  override def affect(service: Service): Unit = {
    service.duration = service.duration + 1
  }

  override def affect(provider: Provider): Unit = {
    //    provider.properties.values.foreach(x => x.value = x.doubleValue * 2)
  }
}
