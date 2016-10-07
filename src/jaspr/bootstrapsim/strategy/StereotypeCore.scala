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

  def makeTrainRow(record: BootRecord): Seq[Any]

  def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any]

  def adverts(provider: Provider): List[Any] = {
    provider.generalAdverts.values.map(_.value.toString).toList
  }

  def adverts(request: ServiceRequest): List[Any] = {
    request.properties.values.map(_.value.toString).toList
  }
}
