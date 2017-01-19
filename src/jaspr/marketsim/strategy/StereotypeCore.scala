package jaspr.marketsim.strategy

import jaspr.core.agent.{AdvertProperties, Provider}

/**
  * Created by phil on 19/01/17.
  */
trait StereotypeCore {

  def adverts(provider: Provider): List[Any] = {
    provider.adverts.values.map(_.value.toString).toList
  }
}
