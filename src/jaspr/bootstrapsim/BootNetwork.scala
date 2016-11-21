package jaspr.bootstrapsim

import jaspr.bootstrapsim.agent.{Truster, Trustee}
import jaspr.core.agent.{Agent, Client, Provider}
import jaspr.core.provenance.Record
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

  override def utility(): Double = {
    (clients.map(_.utility).sum + departedClients.map(_.utility).sum) / (simulation.round * clients.size)
  }

  override def gatherProvenance[T <: Record](agent: Agent): Seq[T] = {
    val availableAdvisors =
      if (simulation.config.advisorsAvailable >= 1d) {
        Chooser.sample(clients.filter(_ != agent), simulation.config.advisorsAvailable.toInt)
      } else {
        clients.withFilter(_ != agent && Chooser.randomBoolean(simulation.config.advisorsAvailable))
      }
    availableAdvisors.flatMap(_.getProvenance[T](agent))
  }


  override def possibleRequests(context: ClientContext): Seq[ServiceRequest] = {
    val availableProviders =
      if (simulation.config.trusteesAvailable >= 1d) {
        Chooser.sample(providers.filter(_.capableOf(context.payload, 0)), simulation.config.trusteesAvailable.toInt)
      } else {
        providers.withFilter(_.capableOf(context.payload, 0) && Chooser.randomBoolean(simulation.config.trusteesAvailable))
      }
    val requests = availableProviders.map(x =>
        simulation.config.request(context, x)
      )
    if (requests.isEmpty) possibleRequests(context)
    else requests
  }

  override def tick(): Unit = {
    _clients = clients.map(x =>
      Chooser.ifHappens(simulation.config.trustorLeaveLikelihood)({
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
