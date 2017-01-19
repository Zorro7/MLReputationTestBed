package jaspr.marketsim.strategy

import jaspr.core.agent.Client
import jaspr.core.provenance.{RatingRecord, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.StrategyInit
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.Chooser
import weka.classifiers.Classifier

/**
  * Created by phil on 19/01/17.
  */
class BRSLike(val witnessWeight: Double = 2d,
              val baseLearner: Classifier,
              override val numBins: Int,
              override val lower: Double,
              override val upper: Double) extends StrategyCore with MlrCore {

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init: BRSLikeInit = baseInit.asInstanceOf[BRSLikeInit]
    (init.trustModel) match {
      case None => new TrustAssessment(baseInit.context, request, Chooser.randomDouble(0,1))
      case Some(trustModel) =>
        val row = makeTestRow(init, request)
        val query = convertRowToInstance(row, trustModel.attVals, trustModel.train)
        val result = makePrediction(query, trustModel)

        new TrustAssessment(baseInit.context, request, result)
    }
  }

  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val directRecords = getDirectRecords(network, context)
    val witnessRecords = getWitnessRecords(network, context)

    if (directRecords.isEmpty && witnessRecords.isEmpty) {
      new BRSLikeInit(context, None)
    } else {
      val trustModel: MlrModel = makeMlrsModel(directRecords ++ witnessRecords, baseLearner, makeTrainRow)

      new BRSLikeInit(context, Some(trustModel))
    }
  }

  def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    0d ::
      request.provider.name ::
      Nil
  }

  def makeTrainRow(record: ServiceRecord with RatingRecord): Seq[Any] = {
    record.rating ::
      record.service.request.provider.name ::
      Nil
  }
}


class BRSContextLike(witnessWeight: Double = 2d,
                     baseLearner: Classifier,
                     numBins: Int,
                     lower: Double,
                     upper: Double) extends BRSLike(witnessWeight, baseLearner, numBins, lower, upper) {

  override def makeTrainRow(record: ServiceRecord with RatingRecord): Seq[Any] = {
    super.makeTrainRow(record) :+ record.service.request.payload.name
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    super.makeTestRow(init, request) :+ request.payload.name
  }
}