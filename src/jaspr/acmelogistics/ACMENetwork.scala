package jaspr.acmelogistics

import jaspr.acmelogistics.agent._
import jaspr.core.Network
import jaspr.core.agent._
import jaspr.core.service.{Service, ServiceRequest, ClientContext}
import jaspr.utilities.Chooser

import scala.annotation.tailrec

/**
 * Created by phil on 17/03/16.
 */
class ACMENetwork(val simulation: ACMESimulation) extends Network {

  override def utility(): Double = clients.map(_.utility).sum

  def providerCompositions: Iterable[Seq[Provider]] = {
    List.fill(simulation.config.numCompositions)(
      Chooser.choose(this.refineries) ::
        Chooser.choose(this.shippers) ::
        Chooser.choose(this.mines) :: Nil
    ).distinct
  }

  override def possibleRequests(network: Network, context: ClientContext): Seq[ServiceRequest] = {
    @tailrec
    def createComposition(ps: Seq[Provider],
                          context: ClientContext,
                          acc: Seq[ServiceRequest] = Nil,
                          depth: Int = 0): ServiceRequest = {
      if (ps.isEmpty) acc.head
      else {
        val client =
          if (ps.size == 1) context.client
          else ps.drop(1).head.asInstanceOf[Client]
        createComposition(
          ps.drop(1), context,
          new ServiceRequest(
            client, ps.head,
            context.round+depth*simulation.config.defaultServiceDuration,
            simulation.config.defaultServiceDuration,
            context.payload,
            markets.head,
            acc
          ) :: Nil,
          depth+1
        )
      }
    }
    providerCompositions.map(x => {
      createComposition(x.reverse, context)
    }).toSeq
  }

  override def events(): Seq[Event] =
    Chooser.ifHappens(0.05)(
      new ACMEEvent(
        Chooser.sample(providers, Chooser.randomInt(0, agents.size/4))
      ) :: Nil
    )(Nil)

  override def agents: Seq[Agent] = clients ++ providers

  override val markets: Seq[Market] = new ACMEMarket(simulation) :: Nil
  override val clients: Seq[Client] = List.fill(simulation.config.numClients)(
    new ACME(simulation)
  )

  override def providers: Seq[Provider] = mines ++ shippers ++ refineries

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
