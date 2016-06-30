package jaspr.sellerssim.strategy.general

import jaspr.core.Network
import jaspr.core.agent.{Provider, Client}
import jaspr.core.provenance.{Record, RatingRecord, TrustAssessmentRecord, ServiceRecord}
import jaspr.core.service.{TrustAssessment, ServiceRequest, ClientContext}
import jaspr.core.strategy.{StrategyInit, Exploration}
import jaspr.sellerssim.strategy.MlrsCore
import jaspr.strategy.CompositionStrategy
import jaspr.utilities.Dirichlet
import weka.classifiers.Classifier
import weka.classifiers.bayes.NaiveBayes

/**
 * Created by phil on 30/06/16.
 */
class HabitLike extends CompositionStrategy with Exploration with MlrsCore {

  override val explorationProbability: Double = 0.1
  override val numBins: Int = 2

  val baseLearner: Classifier = new NaiveBayes

  class HabitLikeInit(context: ClientContext,
                       val trustModel: Option[MlrsModel],
                       val witnessRecords: Seq[ServiceRecord with TrustAssessmentRecord with RatingRecord],
                       val witnesses: Seq[(Client,Provider)]
                        ) extends StrategyInit(context)

  trait Opinions {
    val opinions: Map[(Client,Provider), Dirichlet]
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords: Seq[ServiceRecord with TrustAssessmentRecord with RatingRecord] = context.client.getProvenance(context.client)
    val witnessRecords: Seq[ServiceRecord with TrustAssessmentRecord with RatingRecord] = network.gatherProvenance(context.client)

    val witnesses = witnessRecords.map(x => (x.service.request.client,x.service.request.provider)).sortBy(x => x._1.id+"_"+x._2.id)

    if (directRecords.isEmpty) {
      new HabitLikeInit(context, None, witnessRecords, witnesses)
    } else {
      val trustModel = makeMlrsModel(directRecords, baseLearner, makeTrainRow(_: Record, witnesses))
      new HabitLikeInit(context, Some(trustModel), witnessRecords, witnesses)
    }
  }

  override def compute(init: StrategyInit, request: ServiceRequest): TrustAssessment = ???

  def makeTrainRow(baseRecord: Record, witnesses: Seq[(Client,Provider)]): Seq[Any] = {
    val record = baseRecord.asInstanceOf[ServiceRecord with TrustAssessmentRecord with RatingRecord]
    val opinions: Map[(Client,Provider),Dirichlet] = record.assessment.asInstanceOf[Opinions].opinions
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.service.request.provider.id.toString :: // provider identifier
      witnesses.map(x => opinions.getOrElse(x, new Dirichlet(numBins))).map(x=> x.alpha).toList.flatten
  }

  def makeTestRow(init: HabitLikeInit, request: ServiceRequest, opinions: Map[(Client,Provider),Dirichlet]): Seq[Any] = {
    0d ::
      request.provider.id.toString ::
      init.witnesses.map(x => opinions.getOrElse(x, new Dirichlet(numBins))).map(x=> x.alpha).toList.flatten
  }

  def makeDirichlet(ratings: Seq[Double]): Dirichlet = {
    val prior = new Dirichlet(numBins)
    prior.observe(ratings.map(discretizeDouble))
  }


  def makeWitnessDirichlets(records: Iterable[ServiceRecord with RatingRecord]): Map[(Client,Provider), Dirichlet] = {
    records.groupBy(x =>
      (x.service.request.client,x.service.request.provider) // group by witness agent
    ).mapValues[Dirichlet](x =>
      makeDirichlet(x.map(y => y.rating).toSeq)
    )
  }

}