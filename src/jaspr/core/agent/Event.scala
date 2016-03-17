package jaspr.core.agent

import jaspr.core.service.Service
import jaspr.utilities.{Tickable, NamedEntity}

/**
 * Created by phil on 17/03/16.
 */
trait Event extends NamedEntity with Tickable {

  val providers: Seq[Provider]

  def tick(): Unit = {
    jaspr.debug("EVENT:: ", name, providers)
    for (provider <- providers) {
      affect(provider)
      for (service <- provider.currentServices) {
        affect(service)
        service.serviceContext.addEvent(this)
      }
    }
  }

  def affect(service: Service)
  def affect(provider: Provider)
}
