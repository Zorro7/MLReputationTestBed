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
class BRS(override val witnessWeight: Double = 2d,
          weightWitnessOpinions: Boolean = false,
          override val explorationProbability: Double = 0.1
         ) extends CompositionStrategy with Exploration with BRSCore {

  override val prior: Double = 0.5
//  override val witnessWeight: Double = 3d
  override val goodOpinionThreshold: Double = 0.7
  override val badOpinionThreshold: Double = 0.3

  override val name: String = this.getClass.getSimpleName+"-"+weightWitnessOpinions
  
  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[BRSInit]

    val direct = init.directBetas.getOrElse(request.provider, new BetaDistribution(1,1)) // 1,1 for uniform
    val opinions = init.witnessBetas.values.map(
      _.getOrElse(request.provider, new BetaDistribution(0,0)) // 0,0 if the witness had no information about provider
    )

    val beta = getCombinedOpinions(direct, opinions, witnessWeight)
    val score = beta.belief + prior*beta.uncertainty()

    new TrustAssessment(init.context, request, score)
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords: Seq[BootRecord] = getDirectRecords(network, context)
    val witnessRecords: Seq[BootRecord] = getWitnessRecords(network, context)

    val directBetas: Map[Provider,BetaDistribution] = makeDirectBetas(directRecords)
    val witnessBetas: Map[Client, Map[Provider, BetaDistribution]] = makeWitnessBetas(witnessRecords)

    val weightedWitnessBetas: Map[Client, Map[Provider, BetaDistribution]] =
      if (weightWitnessOpinions) weightWitnessBetas(witnessBetas, directBetas)
      else witnessBetas

    new BRSInit(context, directBetas, weightedWitnessBetas)
  }
}
