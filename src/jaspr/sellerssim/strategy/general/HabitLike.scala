package jaspr.sellerssim.strategy.general

import jaspr.core.agent.{Client, Provider}
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.CompositionStrategy
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.Dirichlet
import weka.classifiers.Classifier

/**
  * Created by phil on 30/06/16.
  */
class HabitLike(val baseLearner: Classifier, override val numBins: Int) extends CompositionStrategy with Exploration with MlrCore {

  override val explorationProbability: Double = 0d


  class HabitLikeInit(context: ClientContext,
                      val trustModel: Option[MlrModel],
                      val witnessRecords: Seq[ServiceRecord with TrustAssessmentRecord with RatingRecord],
                      val witnesses: Seq[(Client, Provider)]
                     ) extends StrategyInit(context)

  trait Opinions {
    val opinions: Map[(Client, Provider), Dirichlet]
  }

  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val directRecords: Seq[ServiceRecord with TrustAssessmentRecord with RatingRecord] = context.client.getProvenance(context.client)
    val witnessRecords: Seq[ServiceRecord with TrustAssessmentRecord with RatingRecord] = network.gatherProvenance(context.client)

    val witnesses = witnessRecords.map(x => (x.service.request.client, x.service.request.provider)).sortBy(x => x._1.id + "_" + x._2.id)

    if (directRecords.isEmpty) {
      new HabitLikeInit(context, None, witnessRecords, witnesses)
    } else {
      val trustModel = makeMlrsModel(directRecords, baseLearner, makeTrainRow(_: Record, witnesses))
      new HabitLikeInit(context, Some(trustModel), witnessRecords, witnesses)
    }
  }

  override def compute(init: StrategyInit, request: ServiceRequest): TrustAssessment = ???

  def makeTrainRow(baseRecord: Record, witnesses: Seq[(Client, Provider)]): Seq[Any] = {
    val record = baseRecord.asInstanceOf[ServiceRecord with TrustAssessmentRecord with RatingRecord]
    val opinions: Map[(Client, Provider), Dirichlet] = record.assessment.asInstanceOf[Opinions].opinions
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.service.request.provider.id.toString :: // provider identifier
      witnesses.map(x => opinions.getOrElse(x, new Dirichlet(numBins))).map(x => x.alpha).toList.flatten
  }

  def makeTestRow(init: HabitLikeInit, request: ServiceRequest, opinions: Map[(Client, Provider), Dirichlet]): Seq[Any] = {
    0d ::
      request.provider.id.toString ::
      init.witnesses.map(x => opinions.getOrElse(x, new Dirichlet(numBins))).map(x => x.alpha).toList.flatten
  }

  def makeDirichlet(ratings: Seq[Double]): Dirichlet = {
    val prior = new Dirichlet(numBins)
    prior.observe(ratings.map(discretizeDouble))
  }


  def makeWitnessDirichlets(records: Iterable[ServiceRecord with RatingRecord]): Map[(Client, Provider), Dirichlet] = {
    records.groupBy(x =>
      (x.service.request.client, x.service.request.provider) // group by witness agent
    ).mapValues[Dirichlet](x =>
      makeDirichlet(x.map(y => y.rating).toSeq)
    )
  }

}
