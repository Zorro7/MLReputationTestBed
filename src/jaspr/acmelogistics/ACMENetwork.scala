package jaspr.acmelogistics

import jaspr.acmelogistics.agent._
import jaspr.core.Network
import jaspr.core.agent._
import jaspr.core.service.{ServiceRequest, ClientContext}

/**
 * Created by phil on 17/03/16.
 */
class ACMENetwork(val simulation: ACMESimulation) extends Network {

  override def utility(): Double = clients.map(_.utility).sum

  override def possibleRequests(network: Network, context: ClientContext): Seq[ServiceRequest] = {
    network.providers.map(
      new ServiceRequest(context.client, _, context.round, 1, context.payload, context.market)
    )
  }

  override def events(): Seq[Event] = Nil

  override def agents: Seq[Agent] = clients ++ providers

  override val markets: Seq[Market] = new ACMEMarket(simulation) :: Nil
  override val clients: Seq[Client] = List.fill(simulation.config.numClients)(
    new ACME(simulation)
  )

  override def providers: Seq[Provider] = shippers ++ mines ++ refineries

  var shippers: List[Shipper] = List.fill(simulation.config.numShippers)(
    new Shipper(simulation)
  )

  var refineries: List[Refinery] = List.fill(simulation.config.numRefineries)(
    new Refinery(simulation)
  )

  var mines: List[Mine] = List.fill(simulation.config.numMines)(
    new Mine(simulation)
  )
}
