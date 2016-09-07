package jaspr.strategy.fire

import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.sellerssim.strategy.MlrsCore
import jaspr.strategy.{CompositionStrategy, RatingStrategy}
import jaspr.utilities.Dirichlet
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.rules.OneR

import scala.math._

/**
 * Created by phil on 30/03/16.
 */
class MLFire(val witnessWeight: Double = 0.5) extends CompositionStrategy with Exploration with MlrsCore {

  override val name = this.getClass.getSimpleName+"-"+witnessWeight
  override val numBins: Int = 10

  val baseModel = new OneR

  override val explorationProbability: Double = 0.1

  // In recency scaling, the number of rounds before an interaction rating should be half that of the current round
  val RecencyScalingPeriodToHalf = 5
  // FIRE's recency scaling factor for interaction ratings (lambda)
  val RecencyScalingFactor = -RecencyScalingPeriodToHalf / log(0.5)

  class MLFireInit(context: ClientContext,
                   val directModel: MlrsModel,
                   val witnessModel: MlrsModel
                    ) extends StrategyInit(context)

  override def initStrategy(network: Network, context: ClientContext) = {
    val direct = context.client.getProvenance(context.client)
    val directModel =
      if (direct.isEmpty) null
      else makeMlrsModel(direct, baseModel, makeTrainRows, makeTrainWeight(context, _:ServiceRecord))

    val witnessModel =
      if (witnessWeight == 0d) null
      else {
        val witness = network.gatherProvenance(context.client)
        if (witness.isEmpty) null
        else makeMlrsModel(witness, baseModel, makeTrainRows, makeTrainWeight(context, _:ServiceRecord))
      }

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

    new TrustAssessment(baseInit.context, request, (1-witnessWeight)*direct + witnessWeight*witness)
  }

  def makeTrainWeight(context: ClientContext, record: ServiceRecord): Double = {
//    1d / (context.round - record.asInstanceOf[ServiceRecord].service.end).toDouble
//    weightRating(record.service.end, context.round)
    1d
  }

  def makeTrainRows(record: ServiceRecord with RatingRecord): Seq[Any] = {
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.service.request.end ::
      record.service.request.provider.id.toString :: // provider identifier
      Nil
  }

  def makeTestRow(request: ServiceRequest): List[Any] = {
    0d :: request.start :: request.provider.id.toString :: Nil
  }
}
