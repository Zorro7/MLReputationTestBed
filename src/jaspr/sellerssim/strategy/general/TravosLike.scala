package jaspr.sellerssim.strategy.general

import jaspr.core.agent.Client
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.CompositionStrategy
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.Dirichlet
import weka.classifiers.Classifier

/**
  * Created by phil on 29/06/16.
  */
class TravosLike(val baseLearner: Classifier, override val numBins: Int) extends CompositionStrategy with Exploration with MlrCore {

  override val explorationProbability: Double = 0d

  class TravosLikeInit(context: ClientContext,
                       val trustModel: Option[MlrModel],
                       val witnessRecords: Seq[ServiceRecord with TrustAssessmentRecord with RatingRecord],
                       val witnesses: Seq[Client]
                      ) extends StrategyInit(context)

  trait Opinions {
    val opinions: Map[Client, Dirichlet]
  }

  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val directRecords: Seq[ServiceRecord with TrustAssessmentRecord with RatingRecord] = context.client.getProvenance(context.client)
    val witnessRecords: Seq[ServiceRecord with TrustAssessmentRecord with RatingRecord] = network.gatherProvenance(context.client)

    val witnesses = witnessRecords.map(_.service.request.client).sortBy(_.id)

    if (directRecords.isEmpty) {
      new TravosLikeInit(context, None, witnessRecords, witnesses)
    } else {
      val trustModel = makeMlrsModel(directRecords, baseLearner, makeTrainRow(_: Record, witnesses))
      new TravosLikeInit(context, Some(trustModel), witnessRecords, witnesses)
    }
  }

  override def computeAssessment(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val requestScores: Seq[TrustAssessment with Opinions] = request.flatten().map(x => compute(baseInit, request))
    new TrustAssessment(baseInit.context, request, requestScores.map(_.trustValue).sum) with Opinions {
      override val opinions = requestScores.flatMap(_.opinions).toMap
    }
  }

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment with Opinions = {
    val init: TravosLikeInit = baseInit.asInstanceOf[TravosLikeInit]
    val witnessOpinions = makeWitnessDirichlets(
      init.witnessRecords.filter(_.service.request.provider == request.provider)
    )

    init.trustModel match {
      case None =>
        new TrustAssessment(baseInit.context, request, 0d) with Opinions {
          override val opinions = witnessOpinions
        }
      case Some(trustModel) =>
        val row = makeTestRow(init, request, witnessOpinions)
        val query = convertRowToInstance(row, trustModel.attVals, trustModel.train)
        val pred = trustModel.model.classifyInstance(query)
        val result =
          if (discreteClass && numBins <= 2) {
            val dist = trustModel.model.distributionForInstance(query)
            dist.zipWithIndex.map(x => x._1 * trustModel.train.classAttribute().value(x._2).toDouble).sum
          } else if (discreteClass) {
            val pred = trustModel.model.classifyInstance(query)
            trustModel.train.classAttribute().value(pred.toInt).toDouble
          } else trustModel.model.classifyInstance(query)
        new TrustAssessment(baseInit.context, request, result) with Opinions {
          override val opinions = witnessOpinions
        }
    }
  }

  def makeTrainRow(baseRecord: Record, witnesses: Seq[Client]): Seq[Any] = {
    val record = baseRecord.asInstanceOf[ServiceRecord with TrustAssessmentRecord with RatingRecord]
    val opinions: Map[Client, Dirichlet] = record.assessment.asInstanceOf[Opinions].opinions
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.service.request.provider.id.toString :: // provider identifier
      witnesses.map(x => opinions.getOrElse(x, new Dirichlet(numBins))).map(x => x.alpha).toList.flatten
  }

  def makeTestRow(init: TravosLikeInit, request: ServiceRequest, opinions: Map[Client, Dirichlet]): Seq[Any] = {
    0d ::
      request.provider.id.toString ::
      init.witnesses.map(x => opinions.getOrElse(x, new Dirichlet(numBins))).map(x => x.alpha).toList.flatten
  }

  def makeDirichlet(ratings: Seq[Double]): Dirichlet = {
    val prior = new Dirichlet(numBins)
    prior.observe(ratings.map(discretizeDouble))
  }


  def makeWitnessDirichlets(records: Iterable[ServiceRecord with RatingRecord]): Map[Client, Dirichlet] = {
    records.groupBy(x =>
      x.service.request.client // group by witness agent
    ).mapValues[Dirichlet](x =>
      makeDirichlet(x.map(y => y.rating).toSeq)
    )
  }
}
