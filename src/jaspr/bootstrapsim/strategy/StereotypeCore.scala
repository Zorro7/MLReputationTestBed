package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.BootRecord
import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.ServiceRequest
import jaspr.core.strategy.StrategyInit
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.BetaDistribution
import weka.classifiers.Classifier


/**
  * Created by phil on 06/10/16.
  */
trait StereotypeCore extends MlrCore {

  def computeStereotypeWeight(model: MlrModel, betas: Map[Provider,BetaDistribution]): Double = {
    val sqrdiff = betas.map(b => {
      val exp = b._2.expected()
      val row = 0d :: adverts(b._1)
      val query = convertRowToInstance(row, model.attVals, model.train)
      val pred = makePrediction(query, model)
      (exp-pred)*(exp-pred)
    }).sum
    1-Math.sqrt(sqrdiff / model.train.size.toDouble)
  }

  def makeStereotypeModels(records: Seq[BootRecord],
                           baseLearner: Classifier,
                           makeTrainRow: BootRecord => Seq[Any]
                          ): Map[Client, MlrModel] = {
    records.groupBy(
      _.service.request.client
    ).mapValues(
      rs => makeMlrsModel(rs, baseLearner, makeTrainRow)
    )
  }

  def makeTrainRow(record: BootRecord): Seq[Any] = {
    record.rating :: adverts(record.service.request.provider)
  }

  def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    0d :: adverts(request.provider)
  }

  def adverts(provider: Provider) = {
    provider.generalAdverts.values.map(_.value.toString).toList
  }
}
