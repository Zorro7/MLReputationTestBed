package jaspr.marketsim.strategy

import jaspr.core.agent.AdvertProperties
import jaspr.core.service.Payload

/**
  * Created by phil on 19/01/17.
  */
trait ContextCore {

  def context(payload: Payload): List[Any] = {
    val x =payload match {
      case p: AdvertProperties =>
        if (p.adverts.isEmpty) p.name :: Nil
        else p.adverts.values.map(_.value.toString).toList
      case p => p.name :: Nil
    }
//    println(x)
    x
  }
}
