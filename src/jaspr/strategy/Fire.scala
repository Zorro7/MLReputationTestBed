package jaspr.strategy

import jaspr.core.Network
import jaspr.core.agent.Provider
import jaspr.core.provenance.{ServiceRecord, Record}
import jaspr.core.service.{TrustAssessment, ServiceRequest, ClientContext}
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
    val direct = context.client.getProvenance[ServiceRecord].map(x =>
      new Rating(x.service.request.provider, x.service.utility())
    )
    val witness = context.client.gatherProvenance[ServiceRecord]().map(x =>
      new Rating(x.service.request.provider, x.service.utility())
    )

    new FireStrategyInit(direct, witness)
  }

  override def computeAssessment(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init: FireStrategyInit = baseInit.asInstanceOf[FireStrategyInit]

    val directRecords = init.directRecords.withFilter(_.provider == request.provider).map(_.rating)
    val witnessRecords = init.witnessRecords.filter(_.provider == request.provider).map(_.rating)

    val direct = directRecords.sum / directRecords.size
    val witness = witnessRecords.sum / witnessRecords.size

    new TrustAssessment(request, direct + witness)
  }

  override def possibleRequests(network: Network, context: ClientContext): Seq[ServiceRequest] = {
    network.providers.map(
      new ServiceRequest(context.client, _, context.round, 0, context.properties)
    )
  }

  override val explorationProbability: Double = 0.2
}
