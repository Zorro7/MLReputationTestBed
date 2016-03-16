package jaspr.strategy

import jaspr.core.Network
import jaspr.core.provenance.{ServiceRecord, Record}
import jaspr.core.service.{TrustAssessment, ServiceRequest, ClientContext}
import jaspr.core.strategy.{Exploration, NoExploration, StrategyInit, Strategy}

/**
 * Created by phil on 16/03/16.
 */

class FireStrategyInit(val directRecords: Iterable[ServiceRecord],
                       val witnessRecords: Iterable[ServiceRecord]
                        ) extends StrategyInit

class Fire extends Strategy with Exploration {

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    new FireStrategyInit(context.client.getProvenance, context.client.gatherProvenance())
  }

  override def computeAssessment(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init: FireStrategyInit = baseInit.asInstanceOf[FireStrategyInit]
    val directRecords = init.directRecords.filter(
      _.service.request.provider == request.provider
    )
    val direct = directRecords.map(_.service.utility()).sum / directRecords.size
    val witnessRecords = init.witnessRecords.filter(
      _.service.request.provider == request.provider
    )
    val witness = witnessRecords.map(_.service.utility()).sum / witnessRecords.size
    new TrustAssessment(request, direct + witness)
  }

  override def possibleRequests(network: Network, context: ClientContext): Seq[ServiceRequest] = {
    network.providers.map(
      new ServiceRequest(context.client, _, context.round, 1, context.properties)
    )
  }

  override val explorationProbability: Double = 0.2
}
