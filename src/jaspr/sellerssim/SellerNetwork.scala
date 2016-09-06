package jaspr.sellerssim

import jaspr.core.provenance.Record
import jaspr.core.Network
import jaspr.core.agent._
import jaspr.core.service.{ServiceRequest, ClientContext}
import jaspr.sellerssim.agent.{SellerMarket, Seller, Buyer}
import jaspr.utilities.Chooser

/**
 * Created by phil on 21/03/16.
 */
class SellerNetwork(override val simulation: SellerSimulation) extends Network {

  override def events(): Seq[Event] = Nil

  override def agents: Seq[Agent] = clients ++ providers

  override val clients: Seq[Client] = List.fill(simulation.config.numClients)(
    new Buyer(simulation)
  )

  override def utility(): Double = clients.map(_.utility).sum

  override val providers: Seq[Provider] = List.fill(simulation.config.numProviders)(
    new Seller(simulation)
  )

  override def possibleRequests(context: ClientContext): Seq[ServiceRequest] = {
    val p = providers.withFilter(
      _.capableOf(context.payload, 0)// && Chooser.nextDouble() < simulation.config.clientInvolvementLikelihood
    ).map(x =>
      new ServiceRequest(
        context.client, x, simulation.round, 0, context.payload, context.market
      )
    )
    if (p.isEmpty) possibleRequests(context)
    else p
  }

  override def gatherProvenance[T <: Record](agent: Agent): Seq[T] = {
    clients.withFilter(
      _ != agent && Chooser.nextDouble() < simulation.config.witnessRequestLikelihood
    ).flatMap(_.getProvenance[T](agent))
  }

  override def markets: Seq[Market] = new SellerMarket(simulation) :: Nil

}
