package jaspr.sellerssim.strategy.general

import jaspr.core.agent.Client
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.StrategyInit
import jaspr.utilities.Dirichlet
import weka.classifiers.Classifier

/**
  * Created by phil on 30/06/16.
  */
class TravosLikeSlim(baseLearner: Classifier, numBins: Int) extends TravosLike(baseLearner, numBins) {

  override val explorationProbability: Double = 0.1

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords: Seq[ServiceRecord with TrustAssessmentRecord with RatingRecord] = context.client.getProvenance(context.client)
    val witnessRecords: Seq[ServiceRecord with TrustAssessmentRecord with RatingRecord] = network.gatherProvenance(context.client)

    val witnesses = witnessRecords.map(_.service.request.client).sortBy(_.id)

    if (directRecords.isEmpty) {
      new TravosLikeInit(context, None, witnessRecords, witnesses)
    } else {
      val trustModel = makeMlrsModel(directRecords, baseLearner, makeTrainRows(_: Record, witnesses).flatten)
      new TravosLikeInit(context, Some(trustModel), witnessRecords, witnesses)
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
        val rows = makeTestRows(init, request, witnessOpinions)
        val queries = convertRowsToInstances(rows, trustModel.attVals, trustModel.train)
        val preds = queries.map(trustModel.model.classifyInstance)
        val result =
          if (discreteClass) preds.map(x => trustModel.train.classAttribute().value(x.toInt).toDouble).sum / preds.size
          else preds.sum / preds.size
        new TrustAssessment(baseInit.context, request, result) with Opinions {
          override val opinions = witnessOpinions
        }
    }
  }

  def makeTrainRows(baseRecord: Record, witnesses: Seq[Client]): Seq[Seq[Any]] = {
    val record = baseRecord.asInstanceOf[ServiceRecord with TrustAssessmentRecord with RatingRecord]
    val opinions: Map[Client, Dirichlet] = record.assessment.asInstanceOf[Opinions].opinions
    val direct = (if (discreteClass) discretizeInt(record.rating) else record.rating) ::
      record.service.request.provider.id.toString ::
      Nil
    witnesses.map(x => direct ++
      (x.id :: opinions.getOrElse(x, new Dirichlet(numBins)).alpha.toList)
    )
  }

  def makeTestRows(init: TravosLikeInit, request: ServiceRequest, opinions: Map[Client, Dirichlet]): Seq[Seq[Any]] = {
    val direct = 0d :: request.provider.id.toString :: Nil
    init.witnesses.map(x => direct ++
      (x.id :: opinions.getOrElse(x, new Dirichlet(numBins)).alpha.toList)
    )
  }

}
