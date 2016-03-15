package jaspr.core

import jaspr.core.agent.Agent

/**
 * Created by phil on 15/03/16.
 */
abstract class Network {

  def utility(): Double
  def agents(): Iterable[Agent]
}
