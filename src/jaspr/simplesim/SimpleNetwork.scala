package jaspr.simplesim

import jaspr.core.agent.{Agent, Client, Provider}
import jaspr.core.{Network, Simulation}
import jaspr.simplesim.agent.SimpleAgent

/**
 * Created by phil on 15/03/16.
 */
class SimpleNetwork(val simulation: Simulation) extends Network {

  override def utility(): Double = agents.map(_.utility).sum

  override val agents: Seq[Agent] = List.fill(1)(new SimpleAgent(simulation))

  override val clients: Seq[Client] = agents.map(_.asInstanceOf[SimpleAgent])
  override val providers: Seq[Provider] = agents.map(_.asInstanceOf[SimpleAgent])
}
