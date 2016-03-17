package jaspr.simplesim

import jaspr.core.agent._
import jaspr.core.{Network}
import jaspr.simplesim.agent.{SimpleMarket, SimpleEvent, SimpleAgent}
import jaspr.utilities.Chooser

/**
 * Created by phil on 15/03/16.
 */
class SimpleNetwork(val simulation: SimpleSimulation) extends Network {

  override def utility(): Double = agents.map(_.utility).sum

  override val agents: Seq[Agent] = List.fill(simulation.config.numAgents)(new SimpleAgent(simulation))

  override val clients: Seq[SimpleAgent] = agents.map(_.asInstanceOf[SimpleAgent])
  override val providers: Seq[SimpleAgent] = agents.map(_.asInstanceOf[SimpleAgent])

  override val markets: Seq[Market] = new SimpleMarket(simulation) :: Nil

  override def events(): Seq[Event] = {
    Chooser.ifHappens(0.1)(SimpleEvent("Event", Chooser.sample(providers, 2)) :: Nil)(Nil)
  }
}
