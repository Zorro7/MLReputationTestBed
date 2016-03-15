package jaspr.core

import jaspr.core.agent.{Provider, Client, Agent}

/**
 * Created by phil on 15/03/16.
 */
abstract class Network {

  def utility(): Double
  def agents: Seq[Agent]
  def clients: Seq[Client]
  def providers: Seq[Provider]
}
