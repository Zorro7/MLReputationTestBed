package jaspr.simplesim

import jaspr.core.{Network, Simulation}
import jaspr.simplesim.agent.SimpleAgent

/**
 * Created by phil on 15/03/16.
 */
class SimpleNetwork(val simulation: Simulation) extends Network {

  override def utility(): Double = agents.map(_.utility).sum

  override val agents = List.fill(3)(new SimpleAgent(simulation))
}
