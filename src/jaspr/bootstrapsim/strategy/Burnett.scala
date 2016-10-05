package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.BootRecord
import jaspr.core.agent.{Client, Provider}
import jaspr.core.provenance.{RatingRecord, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.betareputation.BetaCore
import jaspr.strategy.mlr.{MlrModel, MlrCore}
import jaspr.strategy.{CompositionStrategy, RatingStrategy}
import jaspr.utilities.BetaDistribution
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.Classifier

/**
  * Created by phil on 30/09/2016.
  */
class Burnett(baseLearner: Classifier,
              override val numBins: Int,
              val witnessWeight: Double = 2d,
              override val explorationProbability: Double = 0.1
             ) extends CompositionStrategy with Exploration with BetaCore with MlrCore {


  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[BurnettInit]

    val betas = init.witnessBetas.values.map(x =>
      x.get(request.provider) match {
        case Some(dist) => dist
        case None => new BetaDistribution()
      }
    )
    val betadist = getCombinedOpinions(new BetaDistribution(), betas)
    val belief = betadist.expected()
    val uncert = betadist.uncertainty()*2 // Burnett's paper states 2/(r+s+2) where r and s are [un]successful interactions

//    val stereotype = 0.5
//      if (init.stereotypeModels.isEmpty) 0.5
//      else {
//        val gatheredStereotypes = init.stereotypeModels.map(x => {
//          val row = makeTestRow(init, x._1, request)
//          val query = convertRowToInstance(row, x._2.attVals, x._2.train)
//          makePrediction(query, x._2) //* init.RMSEs(x._1)
//        })
//        gatheredStereotypes.sum / gatheredStereotypes.size.toDouble + 1
//      }

    val score = belief //+ stereotype * uncert

    new TrustAssessment(init.context, request, score)
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords: Seq[BootRecord] =
      context.client.getProvenance[BootRecord](context.client)
    val witnessRecords: Seq[BootRecord] =
      if (witnessWeight == 0) Nil
      else network.gatherProvenance[BootRecord](context.client)
    val records = directRecords ++ witnessRecords
    val witnesses: Seq[Client] = witnessRecords.map(_.service.request.client).distinct
    val trustees: Seq[Provider] = records.map(_.service.request.provider).distinct

    if (directRecords.isEmpty && witnessRecords.isEmpty) {
      new BurnettInit(context, Map(), Map(), Map(), Map())
    } else {
      val stereotypeModels: Map[Client,MlrModel] = records.groupBy(
        _.service.request.client
      ).mapValues(
        rs => makeMlrsModel(rs, baseLearner, makeTrainRow)
      )

      val directBetas: Map[Provider,BetaDistribution] =
        if (witnessWeight != 1) {
          directRecords.groupBy(
            _.service.request.provider
          ).mapValues(
            rs => makeBetaDistribution(rs.map(_.success))
          )
        } else {
          Map()
        }

      val witnessBetas: Map[Client, Map[Provider, BetaDistribution]] =
        if (witnessWeight > 0) {
          witnessRecords.groupBy(
            _.service.request.client
          ).mapValues(
            x => x.groupBy(
              _.service.request.provider
            ).mapValues(
              rs => makeBetaDistribution(rs.map(_.success))
            )
          )
        } else {
          Map()
        }

//      val RMSEs: Map[Client,Double] = stereotypeModels.map(sm => {
//        val model = sm._2
//        val witness = sm._1
//        val sqdiff = betas(witness).map(b => {
//          val exp = b._2.expected()
//          val row = 0d :: witness.name :: b._1.generalAdverts.values.map(_.value.toString).toList
//          val query = convertRowToInstance(row, model.attVals, model.train)
//          val pred = makePrediction(query, model)
//          (exp-pred)*(exp-pred)
//        }).sum
//        witness -> Math.sqrt(sqdiff / model.train.size.toDouble)
//      })

      new BurnettInit(
        context,
        directBetas,
        witnessBetas,
        Map(),
        Map()
//        RMSEs
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
