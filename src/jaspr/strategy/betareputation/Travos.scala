package jaspr.strategy.betareputation

import jaspr.core.Network
import jaspr.core.agent.{Client, Agent}
import jaspr.core.provenance.{Record, RatingRecord, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.{TrustAssessment, ClientContext, ServiceRequest}
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.{CompositionStrategy, Rating, RatingStrategy}
import jaspr.utilities.BetaDistribution

/**
 * Created by phil on 11/02/16.
 */
class Travos extends RatingStrategy with CompositionStrategy with Exploration with BetaCore with TravosCore {

  val explorationProbability = 0.1
  val numBins = 5
  val confidenceThreshold = 1
  val eps = 0.1


  override def initStrategy(network: Network, context: ClientContext) = {
    val direct = context.client.getProvenance[ServiceRecord with RatingRecord with TrustAssessmentRecord](context.client).map(x =>
        new Rating(
          x.service.request.client,
          x.service.request.provider,
          x.service.end,
          x.rating
        ) with BetaOpinions {
          override val opinions: List[(Agent,BetaDistribution)] =
            x.assessment match {
              case ass: TravosTrustAssessment =>
                ass.opinions.getOrElse(x.service.request, new BetaOpinions {
                  override val opinions: List[(Agent, BetaDistribution)] = Nil
                }).opinions
              case ass: TrustAssessment =>
                Nil
            }
        }
      )
    val witness = toRatings(network.gatherProvenance(context.client))
    new TravosInit(
      context,
      direct,
      witness,
      getObservations(direct)
    )
  }

  override def computeAssessment(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val requestScores: Seq[TravosTrustAssessment] = request.flatten().map(x => compute(baseInit, request))
    new TravosTrustAssessment(baseInit.context, request, requestScores.map(_.trustValue).sum, requestScores.flatMap(_.opinions).toMap)
  }


  override def compute(baseInit: StrategyInit, request: ServiceRequest): TravosTrustAssessment = {
    val init = baseInit.asInstanceOf[TravosInit]

    val interactionTrust = makeBetaDistribution(init.directRecords.filter(
      _.provider == request.provider
    ).map(_.success))
    val interactionConfidence: Double =
      interactionTrust.integrate(interactionTrust.expected - eps, interactionTrust.expected + eps)

    val witnessOpinions: Map[Client,BetaDistribution] =
      if (interactionConfidence < confidenceThreshold) // if interaction trust confidence is low, use witness opinions
        makeWitnessBetaDistribution(
          init.witnessRecords.filter(_.provider == request.provider)
        )
      else Map()

    // Observations in client's provenance about witness opinion providers
    val observations: Map[Agent, Seq[(Boolean, BetaDistribution)]] =
      if (interactionConfidence < confidenceThreshold) // if interaction trust confidence is low, use witness opinions
        init.asInstanceOf[TravosInit].observations
      else
        Map()

    // weight the witness opinions by their expected accuracy
    val weightedOpinions: Iterable[BetaDistribution] = witnessOpinions.map(x =>
      weightOpinion(x._2, observations.getOrElse(x._1, List()), numBins
    ))

    val combinedOpinions = getCombinedOpinions(interactionTrust, weightedOpinions)

    new TravosTrustAssessment(baseInit.context, request, combinedOpinions.expected(), Map(request -> new BetaOpinions {
      override val opinions: List[(Agent, BetaDistribution)] = witnessOpinions.toList
    }))
  }

}
