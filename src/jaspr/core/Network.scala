package jaspr.core

import jaspr.core.agent._

/**
 * Created by phil on 15/03/16.
 */
abstract class Network {

  val simulation: Simulation

  def utility(): Double
  def markets: Seq[Market]
  def agents: Seq[Agent]
  def clients: Seq[Client]
  def providers: Seq[Provider]
  def events(): Seq[Event]
}
