package jaspr.marketsim.strategy

import jaspr.core.agent.AdvertProperties
import jaspr.core.service.Payload

/**
  * Created by phil on 19/01/17.
  */
trait ContextCore {

  def context(payload: Payload): List[Any] = {
    payload match {
      case p: AdvertProperties => p.adverts.values.map(_.value.toString).toList
      case p => p.name :: Nil
    }
  }
}
