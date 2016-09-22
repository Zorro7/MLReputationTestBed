package jaspr.sellerssim

import jaspr.core.agent._
import jaspr.core.provenance.Record
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.simulation.{Network, NetworkMarket}
import jaspr.sellerssim.agent.SellerMarket
import jaspr.utilities.Chooser

import scala.annotation.tailrec


abstract class SellerNetwork extends Network with NetworkMarket {
  override val simulation: SellerSimulation

//  override def utility(): Double = clients.map(_.utility).sum
  override def utility: Double = clients.withFilter(_.id % simulation.config.numClients < 5).map(_.utility).sum

  override def agents: Seq[Agent] = clients ++ providers

  override def possibleRequests(context: ClientContext): Seq[ServiceRequest] = {
    if (simulation.config.limitClientsUntilRound > 0 && (context.client.id % simulation.config.numClients) < (simulation.config.numClients/2d)) {
      if (simulation.round < simulation.config.limitClientsUntilRound) {
//        println("Specials.", context.client.name)
        providers.withFilter(x =>
          x.capableOf(context.payload, 0) && (x.id % simulation.config.numProviders) < (simulation.config.numProviders/2d)
        ).map(x =>
          new ServiceRequest(
            context.client, x, simulation.round, 0, context.payload, context.market
          )
        )
      } else {
        providers.withFilter(x =>
          x.capableOf(context.payload, 0) && (x.id % simulation.config.numProviders) >= (simulation.config.numProviders/2d)
        ).map(x =>
          new ServiceRequest(
            context.client, x, simulation.round, 0, context.payload, context.market
          )
        )
      }
    } else {
      providers.withFilter(
        _.capableOf(context.payload, 0)
      ).map(x =>
        new ServiceRequest(
          context.client, x, simulation.round, 0, context.payload, context.market
        )
      )
    }
  }

  override def gatherProvenance[T <: Record](agent: Agent): Seq[T] = {
    clients.withFilter(x =>
      x != agent && (x.id % simulation.config.numClients) >= (simulation.config.numClients / 2d)//Chooser.nextDouble() < simulation.config.witnessRequestLikelihood
    ).flatMap(_.getProvenance[T](agent))
  }

  override def market: Market = new SellerMarket
}