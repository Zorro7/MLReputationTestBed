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
             ) extends CompositionStrategy with Exploration with BRSCore with MlrCore {


  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[BurnettInit]

    val direct = init.directBetas.get(request.provider) match {
      case Some(dist) => dist
      case None => new BetaDistribution(1,1) // 1,1 for uniform
    }

    val opinions = init.witnessBetas.values.map(x =>
      x.get(request.provider) match {
        case Some(dist) => dist
        case None => new BetaDistribution(0,0) // 0,0 if the witness had no information about provider
      }
    )

    val combinedBeta =
      if (witnessWeight == 0 || witnessWeight == 1 || witnessWeight == 2) getCombinedOpinions(direct, opinions)
      else getCombinedOpinions(direct * (1-witnessWeight), opinions.map(_ * witnessWeight))

    val belief = combinedBeta.belief()

    val uncert = combinedBeta.uncertainty()

    val prior =
      if (init.stereotypeModels.isEmpty) 0.5
      else {
        val gatheredStereotypes = init.stereotypeModels.map(x => {
          val row = makeTestRow(init, request)
          val query = convertRowToInstance(row, x._2.attVals, x._2.train)
          makePrediction(query, x._2) * init.RMSEs(x._1)
        })
        gatheredStereotypes.sum / init.RMSEs.values.sum
      }

    val score = belief + prior * uncert

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
        if (witnessWeight != 1) makeOpinions(directRecords, r => r.service.request.provider)
        else Map()

      val witnessBetas: Map[Client, Map[Provider, BetaDistribution]] =
        if (witnessWeight > 0) makeOpinions(witnessRecords, r => r.service.request.client, r => r.service.request.provider)
        else Map()

      val RMSEs: Map[Client,Double] = stereotypeModels.map(sm => {
        val model = sm._2
        val witness = sm._1
        val betas =
          if (context.client == witness) directBetas
          else witnessBetas(witness)
        val sqrdiff = betas.map(b => {
          val exp = b._2.expected()
          val row = 0d :: adverts(b._1)
          val query = convertRowToInstance(row, model.attVals, model.train)
          val pred = makePrediction(query, model)
          (exp-pred)*(exp-pred)
        }).sum
        witness -> (1-Math.sqrt(sqrdiff / model.train.size.toDouble))
      })

      new BurnettInit(
        context,
        directBetas,
        witnessBetas,
        stereotypeModels,
        RMSEs
      )
    }
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
