package jaspr.strategy

import jaspr.core.Network
import jaspr.core.agent.Provider
import jaspr.core.provenance.{ServiceRecord, Record}
import jaspr.core.service.{Payload, TrustAssessment, ServiceRequest, ClientContext}
import jaspr.core.strategy.{Exploration, NoExploration, StrategyInit, Strategy}

/**
 * Created by phil on 16/03/16.
 */


class Rating(val provider: Provider, val rating: Double)

class FireStrategyInit(val directRecords: Seq[Rating],
                       val witnessRecords: Seq[Rating]
                        ) extends StrategyInit

class Fire extends Strategy with Exploration {

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val direct = context.client.getProvenance[ServiceRecord](context.client).map(x =>
      new Rating(x.service.request.provider, x.service.utility())
    )
    val witness = network.gatherProvenance[ServiceRecord](context.client).map(x =>
      new Rating(x.service.request.provider, x.service.utility())
    )

    new FireStrategyInit(direct, witness)
  }

  override def computeAssessment(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init: FireStrategyInit = baseInit.asInstanceOf[FireStrategyInit]

    val requestScores = request.flatten().map(x => fire(x.provider, init.directRecords, init.witnessRecords))

    println(requestScores)
    new TrustAssessment(request, requestScores.sum)
  }

  def fire(provider: Provider, directRecords: Seq[Rating], witnessRecords: Seq[Rating]) = {
    val direct = directRecords.withFilter(_.provider == provider).map(_.rating)
    val witness = witnessRecords.filter(_.provider == provider).map(_.rating)
    direct.sum / (direct.size+1) + witness.sum / (witness.size+1)
  }

  override val explorationProbability: Double = 0.2
}
