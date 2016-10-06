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
class BRS(witnessWeight: Double = 2d,
          weightWitnessOpinions: Boolean = false,
          override val explorationProbability: Double = 0.1
         ) extends CompositionStrategy with Exploration with BRSCore {

  val prior = 0.5

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

    val belief = combinedBeta.belief()
    val uncertainty = combinedBeta.uncertainty()

    val score = belief + prior*uncertainty
    new TrustAssessment(init.context, request, score)
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords: Seq[BootRecord] =
      context.client.getProvenance[BootRecord](context.client)
    val witnessRecords: Seq[BootRecord] =
      if (witnessWeight == 0) Nil
      else network.gatherProvenance[BootRecord](context.client)

    val directBetas: Map[Provider,BetaDistribution] =
      if (witnessWeight != 1) makeOpinions(directRecords, r => r.service.request.provider)
      else Map()

    val witnessBetas: Map[Client, Map[Provider, BetaDistribution]] =
      if (witnessWeight > 0) makeOpinions(witnessRecords, r => r.service.request.client, r => r.service.request.provider)
      else Map()

    val weightedWitnessBetas: Map[Client, Map[Provider, BetaDistribution]] =
      if (weightWitnessOpinions) {
        val goodOpinionThreshold = 0.5

        val witnessWeightings: Map[Client, BetaDistribution] = witnessBetas.map(wb => {
          wb._1 -> wb._2.map(x => {
            val directOpinion = directBetas.getOrElse(x._1, new BetaDistribution(0, 0))
            if (x._2.belief > goodOpinionThreshold) directOpinion
            else new BetaDistribution(directOpinion.beta, directOpinion.alpha) //swap the alphas for agreement with witnessOpinion
          }).foldLeft(new BetaDistribution)(_ + _)
        })

        witnessBetas.map(wb => wb._1 -> {
          val tdist = witnessWeightings(wb._1)
          val t = tdist.belief + 0.5 * tdist.uncertainty
          wb._2.mapValues(x => {
            new BetaDistribution(
              (2 * x.belief() * t) / (1 - x.belief() * t - x.disbelief() * t),
              (2 * x.disbelief() * t) / (1 - x.belief() * t - x.disbelief() * t)
            )
          })
        })
      } else {
        witnessBetas
      }

    new BRSInit(context, directBetas, weightedWitnessBetas)
  }
}
