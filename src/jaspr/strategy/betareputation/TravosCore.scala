package jaspr.strategy.betareputation

import jaspr.core.agent.Agent
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.strategy.{Rating, RatingStrategyInit}
import jaspr.utilities.BetaDistribution

/**
  * Created by phil on 11/02/2016.
  */
trait TravosCore extends BetaCore {

  trait BetaOpinions {
    val opinions: List[(Agent, BetaDistribution)]
  }

  class TravosTrustAssessment(context: ClientContext, request: ServiceRequest, trustValue: Double, val opinions: Map[ServiceRequest, BetaOpinions])
    extends TrustAssessment(context, request, trustValue)

  class TravosInit(context: ClientContext,
                   directRecords: Seq[Rating],
                   witnessRecords: Seq[Rating],
                   val observations: Map[Agent, Seq[(Boolean, BetaDistribution)]]
                  ) extends RatingStrategyInit(context, directRecords, witnessRecords)

  def weightOpinion(opinion: BetaDistribution,
                    observations: Seq[(Boolean, BetaDistribution)],
                    numBins: Int): BetaDistribution = {
    // Get the bin of the opinion raw expected value
    val bin = opinion.getBin(numBins)
    val similarObs: Seq[Boolean] = observations.filter(x =>
      x._2.isInBin(bin, numBins)
    ).map(x => x._1)
    val opObsArea: Double = makeBetaDistribution(similarObs).integrate(
      bin, // Low bin
      bin + (1d / numBins) // High bin
    )
    opinion.getWeighted(opObsArea)
  }

  def getObservations(directRecords: Seq[Rating with BetaOpinions]): Map[Agent, Seq[(Boolean, BetaDistribution)]] = {
    directRecords.flatMap(x =>
      x.opinions.map(obsOp =>
        obsOp._1 -> (x.success, obsOp._2)
      )
    ).groupBy(_._1).mapValues(_.map(_._2))
  }

}
