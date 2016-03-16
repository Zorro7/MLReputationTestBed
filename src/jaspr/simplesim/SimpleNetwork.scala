package jaspr.simplesim

import jaspr.core.agent.{Agent, Client, Provider}
import jaspr.core.{Network}
import jaspr.simplesim.agent.SimpleAgent
import jaspr.utilities.Chooser

/**
 * Created by phil on 15/03/16.
 */
class SimpleNetwork(val simulation: SimpleSimulation) extends Network {

  override def utility(): Double = agents.map(_.utility).sum

  override val agents: Seq[Agent] = List.fill(simulation.config.numAgents)(new SimpleAgent(simulation))

  override val clients: Seq[Client] = agents.map(_.asInstanceOf[SimpleAgent])
  override val providers: Seq[Provider] = agents.map(_.asInstanceOf[SimpleAgent])
}
