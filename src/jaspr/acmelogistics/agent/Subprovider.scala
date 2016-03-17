package jaspr.acmelogistics.agent

import jaspr.core.agent.{Provider, Client}

/**
 * Created by phil on 17/03/16.
 */
abstract class Subprovider extends Client with Provider {

  override def tick(): Unit = {
    super[Provider].tick()
  }

}
