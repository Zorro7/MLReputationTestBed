package jaspr.acmelogistics

import jaspr.acmelogistics.agent.{Mine, Refinery, Shipper, ACME}
import jaspr.core.Network
import jaspr.core.agent.{Event, Agent, Client, Provider}

/**
 * Created by phil on 17/03/16.
 */
class ACMENetwork(val simulation: ACMESimulation) extends Network {

  override def utility(): Double = ???

  override def events(): Seq[Event] = ???

  override def agents: Seq[Agent] = clients ++ providers

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
