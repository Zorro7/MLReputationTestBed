package jaspr.strategy.betareputation

import jaspr.core.agent.{Agent, Client}
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.{ClientContext, Service, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.sellerssim.strategy.MlrsCore
import jaspr.strategy.{CompositionStrategy, Rating, RatingStrategy}
import jaspr.utilities.{BetaDistribution, Dirichlet}
import weka.classifiers.Classifier
import weka.classifiers.bayes.NaiveBayes

import scala.math._

/**
 * Created by phil on 29/06/16.
 */
class MLTravos_provider extends CompositionStrategy with Exploration with MlrsCore with TravosCore with RatingStrategy {
  override val name = this.getClass.getSimpleName
  override val numBins: Int = 10

  val baseModel: Classifier = new NaiveBayes

  override val explorationProbability: Double = 0.1

  // In recency scaling, the number of rounds before an interaction rating should be half that of the current round
  val RecencyScalingPeriodToHalf = 5
  // FIRE's recency scaling factor for interaction ratings (lambda)
  val RecencyScalingFactor = -RecencyScalingPeriodToHalf / log(0.5)

  class MLTravosInit(context: ClientContext,
                     val directRatings: Seq[TravosRating with BetaOpinions],
                     val witnessRatings: Seq[Rating]
                      ) extends StrategyInit(context)

  class TravosRating(val service: Service, val rating: Double) extends Record
  override def initStrategy(network: Network, context: ClientContext) = {
    val direct = context.client.getProvenance[ServiceRecord with RatingRecord with TrustAssessmentRecord](context.client).map(x =>
      new TravosRating(
        x.service,
        x.rating
      ) with BetaOpinions {
        override val opinions: List[(Agent,BetaDistribution)] =
          x.assessment.asInstanceOf[TravosTrustAssessment]
            .opinions.getOrElse(x.service.request, new BetaOpinions {
            override val opinions: List[(Agent, BetaDistribution)] = Nil
          }).opinions
      }
    )

    val witnessReports = toRatings(network.gatherProvenance(context.client))

    new MLTravosInit(context, direct, witnessReports)
  }

  override def computeAssessment(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val requestScores: Seq[TravosTrustAssessment] = request.flatten().map(x => compute(baseInit, request))
    new TravosTrustAssessment(baseInit.context, request, requestScores.map(_.trustValue).sum, requestScores.flatMap(_.opinions).toMap)
  }

  def compute(baseInit: StrategyInit, request: ServiceRequest): TravosTrustAssessment = {
    val init = baseInit.asInstanceOf[MLTravosInit]

    val witnessOpinions: Map[Client,BetaDistribution] =
      makeWitnessBetaDistribution(
        init.witnessRatings.filter(_.provider == request.provider)
      )

    val direct = init.directRatings.filter(x => x.service.request.provider == request.provider)

    val trust =
      if (direct.isEmpty) 0d
      else {
        val witnesses = witnessOpinions.map(_._1).toSeq.sortBy(_.id)
        val travosModel = makeMlrsModel(direct, baseModel, makeTrainRows(_: TravosRating with BetaOpinions, witnesses), makeTrainWeight(init.context, _:TravosRating))
        val testRow = makeTestRow(request, witnessOpinions, witnesses)
        val directQuery = convertRowToInstance(testRow, travosModel.attVals, travosModel.train)
        val x = travosModel.model.distributionForInstance(directQuery)
        undiscretize(new Dirichlet(x).expval())
      }

    new TravosTrustAssessment(baseInit.context, request, trust, Map(request -> new BetaOpinions {
      override val opinions: List[(Agent, BetaDistribution)] = witnessOpinions.toList
    }))
  }

  def makeTrainWeight(context: ClientContext, record: TravosRating): Double = {
    //    1d / (context.round - record.asInstanceOf[ServiceRecord].service.end).toDouble
    //    weightRating(record.service.end, context.round)
    1d
  }

  def makeTrainRows(record: TravosRating with BetaOpinions, witnesses: Seq[Client]): Seq[Any] = {
    val opinions = record.opinions.toMap
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.service.request.provider.id.toString :: // provider identifier
      witnesses.map(x => opinions.getOrElse(x, new BetaDistribution(0,0))).map(x=> x.alpha :: x.beta :: Nil).toList.flatten
  }

  def makeTestRow(request: ServiceRequest, opinions: Map[Client, BetaDistribution], witnesses: Seq[Client]): List[Any] = {
    0d :: request.provider.id.toString ::
      witnesses.map(x => opinions.getOrElse(x, new BetaDistribution(0,0))).map(x=> x.alpha :: x.beta :: Nil).toList.flatten
  }
}
