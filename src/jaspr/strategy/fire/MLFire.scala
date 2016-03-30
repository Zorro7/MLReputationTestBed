package jaspr.strategy.fire

import jaspr.core.Network
import jaspr.core.provenance.{RatingRecord, ServiceRecord, Record}
import jaspr.core.service.{ClientContext, TrustAssessment, ServiceRequest}
import jaspr.core.strategy.{StrategyInit, Exploration}
import jaspr.sellerssim.strategy.MlrsCore
import jaspr.strategy.{CompositionStrategy, RatingStrategy}
import jaspr.utilities.Dirichlet
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.rules.OneR
import scala.math._

/**
 * Created by phil on 30/03/16.
 */
class MLFire extends CompositionStrategy with Exploration with MlrsCore {

  override val numBins: Int = 5

  val baseModel = new OneR

  override val explorationProbability: Double = 0.1

  // In recency scaling, the number of rounds before an interaction rating should be half that of the current round
  val RecencyScalingPeriodToHalf = 5
  // FIRE's recency scaling factor for interaction ratings (lambda)
  val RecencyScalingFactor = -RecencyScalingPeriodToHalf / log(0.5)

  def weightRating(ratingRound: Int, currentRound: Int): Double = {
    pow(E, -((currentRound - ratingRound) / RecencyScalingFactor))
  }

  class MLFireInit(context: ClientContext,
                   val directModel: MlrsModel,
                   val witnessModel: MlrsModel
                    ) extends StrategyInit(context)

  override def initStrategy(network: Network, context: ClientContext) = {
    val direct = context.client.getProvenance(context.client)
    val witness = network.gatherProvenance(context.client)

    val directModel =
      if (direct.isEmpty) null
      else makeMlrsModel(direct, baseModel, makeTrainRows, makeTrainWeight(context, _:ServiceRecord))
    val witnessModel =
      if (witness.isEmpty) null
      else makeMlrsModel(witness, baseModel, makeTrainRows, makeTrainWeight(context, _:ServiceRecord))

    if (directModel != null) println(directModel.model)
    new MLFireInit(context, directModel, witnessModel)
  }

  def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[MLFireInit]

    val testRow = makeTestRow(request)

    val direct =
      if (init.directModel != null) {
        val directQuery = convertRowToInstance(testRow, init.directModel.attVals, init.directModel.train)
        val x = init.directModel.model.distributionForInstance(directQuery)
        undiscretize(new Dirichlet(x).expval())
      } else 0d
    val witness =
      if (init.witnessModel != null) {
        val witnessQuery = convertRowToInstance(testRow, init.witnessModel.attVals, init.witnessModel.train)
        val x = init.witnessModel.model.distributionForInstance(witnessQuery)
        undiscretize(new Dirichlet(x).expval())
      } else 0d

    new TrustAssessment(request, direct + witness)
  }

  def makeTrainWeight(context: ClientContext, record: ServiceRecord): Double = {
//    1d / (context.round - record.asInstanceOf[ServiceRecord].service.end).toDouble
//    weightRating(record.service.end, context.round)
    1d
  }

  def makeTrainRows(record: ServiceRecord with RatingRecord): Seq[Any] = {
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.service.request.provider.id.toString :: // provider identifier
      Nil
  }

  def makeTestRow(request: ServiceRequest): List[Any] = {
    0d :: request.provider.id.toString :: Nil
  }
}
