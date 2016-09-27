package jaspr.bootstrapsim

import jaspr.core.agent.{Agent, Client, Provider}
import jaspr.core.service.{ServiceRequest, ClientContext}
import jaspr.core.simulation.{Simulation, Network}

/**
  * Created by phil on 27/09/2016.
  */
class BootNetwork(override val simulation: Simulation) extends Network {

  override def agents: Seq[Agent] = ???

  override def clients: Seq[Client] = ???

  override def utility(): Double = ???

  override def providers: Seq[Provider] = ???

  override def possibleRequests(context: ClientContext): Seq[ServiceRequest] = ???
}
