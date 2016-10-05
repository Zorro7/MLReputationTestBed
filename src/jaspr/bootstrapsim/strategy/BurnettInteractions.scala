package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.BootRecord
import jaspr.core.agent.Client
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.sellerssim.strategy.general.SingleModelStrategy
import jaspr.strategy.betareputation.BetaCore
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.strategy.{CompositionStrategy, Rating, RatingStrategy}
import jaspr.utilities.Chooser
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.Classifier

/**
  * Created by phil on 30/09/2016.
  */
class BurnettInteractions(baseLearner: Classifier,
                          override val numBins: Int,
                          val witnessWeight: Double = 0.5,
                          override val explorationProbability: Double = 0.1
             ) extends CompositionStrategy with Exploration with RatingStrategy with BetaCore with MlrCore {

  class BurnettInit(context: ClientContext,
                    val stereotypeModel: Option[MlrModel],
                    val directRecords: Seq[BootRecord],
                    val witnessRecords: Seq[BootRecord],
                    val witnesses: Seq[Client]
                   ) extends StrategyInit(context)

  val stereotypeModel = new MultiRegression
  stereotypeModel.setSplitAttIndex(1)
  stereotypeModel.setClassifier(baseLearner)

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[BurnettInit]

    val betadist = makeBetaDistribution(init.directRecords.filter(_.service.request.provider == request.provider).map(_.success))
    val belief = betadist.expected()
    val uncert = betadist.uncertainty() * 2 // Burnett's paper states 2/(r+s+2) where r and s are [un]successful interactions

    val stereotype = init.stereotypeModel match {
      case None => 0.5
      case Some(trustModel) =>
        val gatheredStereotypes = init.witnesses.map(witness => {
          val row = makeTestRow(init, witness, request)
          val query = convertRowToInstance(row, trustModel.attVals, trustModel.train)
          makePrediction(query, trustModel)
        })
        gatheredStereotypes.sum / gatheredStereotypes.size.toDouble+1
    }

    val score = belief + stereotype * uncert

    new TrustAssessment(init.context, request, score)
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords: Seq[BootRecord] =
      context.client.getProvenance[BootRecord](context.client)
    val witnessRecords: Seq[BootRecord] =
      if (witnessWeight == 0) Nil
      else network.gatherProvenance[BootRecord](context.client)
    val witnesses: Seq[Client] = witnessRecords.map(_.service.request.client).distinct

    if (directRecords.isEmpty && witnessRecords.isEmpty) {
      new BurnettInit(context, None, Nil, Nil, Nil)
    } else {
      val stereotypeModel = makeMlrsModel(directRecords ++ witnessRecords, baseLearner, makeTrainRow)
      new BurnettInit(
        context,
        Some(stereotypeModel),
        directRecords,
        witnessRecords,
        witnesses
      )
    }
  }

  def makeTrainRow(record: ServiceRecord with RatingRecord): Seq[Any] = {
    record.rating ::
      record.service.request.client.name ::
      record.service.request.provider.generalAdverts.values.map(_.value.toString).toList
  }

  def makeTestRow(init: StrategyInit, pov: Client, request: ServiceRequest): Seq[Any] = {
    0d :: pov.name :: request.provider.generalAdverts.values.map(_.value.toString).toList
  }

}
