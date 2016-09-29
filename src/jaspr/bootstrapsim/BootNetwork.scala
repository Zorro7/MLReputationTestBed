package jaspr.bootstrapsim

import jaspr.bootstrapsim.agent.{Truster, Trustee}
import jaspr.core.agent.{Agent, Client, Provider}
import jaspr.core.service.{ServiceRequest, ClientContext}
import jaspr.core.simulation.{Simulation, Network}
import jaspr.utilities.{Chooser, Tickable}

/**
  * Created by phil on 27/09/2016.
  */
class BootNetwork(override val simulation: BootSimulation) extends Network with Tickable {

  override def agents: Seq[Agent] = clients ++ providers

  private var departedClients: List[Truster] = Nil
  private var _clients: Seq[Truster] = List.fill(simulation.config.numClients)(
    new Truster(simulation)
  )
  override def clients: Seq[Truster] = _clients

  private var departedProviders: List[Trustee] = Nil
  private var _providers: Seq[Trustee] = List.fill(simulation.config.numProviders)(
    new Trustee(simulation)
  )
  override def providers: Seq[Trustee] = _providers

  override def utility(): Double = clients.map(_.utility).sum + departedClients.map(_.utility).sum

  override def possibleRequests(context: ClientContext): Seq[ServiceRequest] = {
    val requests = providers.withFilter(x =>
      x.capableOf(context.payload, 0) && Chooser.nextDouble() < simulation.config.trusteeAvailableLikleihood
    ).map(x =>
      new ServiceRequest(
        context.client, x, simulation.round, 0, context.payload, context.market
      )
    )
    if (requests.isEmpty) possibleRequests(context)
    else requests
  }

  override def tick(): Unit = {
    _clients = clients.map(x =>
      Chooser.ifHappens(simulation.config.trusterLeaveLikelihood)({
        departedClients = x :: departedClients
        new Truster(simulation)
      })(
        x
      )
    )
    _providers = providers.map(x =>
      Chooser.ifHappens(simulation.config.trusteeLeaveLikelihood)({
        departedProviders = x :: departedProviders
        new Trustee(simulation)
      })(
        x
      )
    )
  }
}
