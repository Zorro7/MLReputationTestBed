package jaspr.sellerssim

import jaspr.core.agent._
import jaspr.core.provenance.Record
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.simulation.{Network, NetworkMarket}
import jaspr.sellerssim.agent.{Buyer, Seller, SellerMarket}
import jaspr.utilities.Chooser

import scala.annotation.tailrec



abstract class SellerNetwork extends Network with NetworkMarket {
  override val simulation: SellerSimulation

  override def utility(): Double = clients.map(_.utility).sum

  override def agents: Seq[Agent] = clients ++ providers

  @tailrec
  override final def possibleRequests(context: ClientContext): Seq[ServiceRequest] = {
    val p = providers.withFilter(
      _.capableOf(context.payload, 0) // && Chooser.nextDouble() < simulation.config.clientInvolvementLikelihood
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

  override def market: Market = new SellerMarket
}