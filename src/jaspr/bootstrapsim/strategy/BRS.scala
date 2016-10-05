package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.BootRecord
import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.CompositionStrategy
import jaspr.strategy.betareputation.BetaCore
import jaspr.utilities.BetaDistribution

/**
  * Created by phil on 05/10/16.
  */
class BRS(witnessWeight: Double = 0.5, override val explorationProbability: Double = 0.1) extends CompositionStrategy with Exploration with BetaCore {

  class BRSInit(context: ClientContext,
                val directBetas: Map[Provider,BetaDistribution],
                val witnessBetas: Map[Client,Map[Provider,BetaDistribution]]
               ) extends StrategyInit(context)

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[BRSInit]

    val direct = init.directBetas.get(request.provider) match {
      case Some(dist) => dist
      case None => new BetaDistribution(1,1) // 1,1 for uniform
    }

    val opinions = init.witnessBetas.values.map(x =>
      x.get(request.provider) match {
        case Some(dist) => dist
        case None => new BetaDistribution(0,0) // 0,0 if the witness had no information about provider
      }
    )

    val combinedBeta =
      if (witnessWeight == 0 || witnessWeight == 1 || witnessWeight == 2) getCombinedOpinions(direct, opinions)
      else getCombinedOpinions(direct * (1-witnessWeight), opinions.map(_ * witnessWeight))

    val belief = combinedBeta.expected()
    new TrustAssessment(init.context, request, belief)
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords: Seq[BootRecord] =
      context.client.getProvenance[BootRecord](context.client)
    val witnessRecords: Seq[BootRecord] =
      if (witnessWeight == 0) Nil
      else network.gatherProvenance[BootRecord](context.client)

    val directBetas: Map[Provider,BetaDistribution] =
      if (witnessWeight != 1) {
        directRecords.groupBy(
          _.service.request.provider
        ).mapValues(
          rs => makeBetaDistribution(rs.map(_.success))
        )
      } else {
        Map()
      }

    val witnessBetas: Map[Client, Map[Provider, BetaDistribution]] =
      if (witnessWeight > 0) {
        witnessRecords.groupBy(
          _.service.request.client
        ).mapValues(
          x => x.groupBy(
            _.service.request.provider
          ).mapValues(
            rs => makeBetaDistribution(rs.map(_.success))
          )
        )
      } else {
        Map()
      }

    new BRSInit(context, directBetas, witnessBetas)
  }
}
