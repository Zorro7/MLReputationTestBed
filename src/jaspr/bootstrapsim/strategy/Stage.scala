package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.BootRecord
import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.CompositionStrategy
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.BetaDistribution
import weka.classifiers.Classifier

/**
  * Created by phil on 05/10/16.
  */
class Stage(baseLearner: Classifier,
            override val numBins: Int,
            val witnessWeight: Double = 2d,
            override val explorationProbability: Double = 0.1
           ) extends CompositionStrategy with Exploration with BRSCore with MlrCore {

  val goodOpinionThreshold = 0.7
  val badOpinionThreshold = 0.3
  val prior = 0.5

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[StageInit]

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

    val combinedBeta = getCombinedOpinions(direct, opinions, witnessWeight)

    val belief = combinedBeta.belief()

    val uncert = combinedBeta.uncertainty()

    val prior =
      if (init.stereotypeModel == null) 0.5
      else {
        val row = makeTestRow(init, request)
        val query = convertRowToInstance(row, init.stereotypeModel.attVals, init.stereotypeModel.train)
        makePrediction(query, init.stereotypeModel)
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

    val stereotypeModel: MlrModel =
      if (directRecords.nonEmpty) {
        makeMlrsModel(directRecords, baseLearner, makeTrainRow)
      } else {
        null
      }

    val directBetas: Map[Provider,BetaDistribution] =
      if (witnessWeight != 1) makeOpinions(directRecords, r => r.service.request.provider)
      else Map()

    val witnessBetas: Map[Client, Map[Provider, BetaDistribution]] =
      if (witnessWeight > 0) makeOpinions(witnessRecords, r => r.service.request.client, r => r.service.request.provider)
      else Map()

    val witnessWeightings: Map[Client,BetaDistribution] = witnessBetas.map(wb => {
      wb._1 -> wb._2.map(x => {
        val directOpinion = directBetas.getOrElse(x._1, new BetaDistribution(0,0))
        if (x._2.belief > goodOpinionThreshold) directOpinion
        else if (x._2.belief < badOpinionThreshold) new BetaDistribution(directOpinion.beta, directOpinion.alpha) //swap the alphas for agreement with witnessOpinion
        else new BetaDistribution(0,0)
      }).foldLeft(new BetaDistribution)(_ + _)
    })

    val weightedWitnessBetas: Map[Client, Map[Provider, BetaDistribution]] = witnessBetas.map(wb => wb._1 -> {
      val tdist = witnessWeightings(wb._1)
      val t = tdist.belief + 0.5*tdist.uncertainty
      wb._2.mapValues(x => {
        new BetaDistribution(
          (2*x.belief()*t)/(1 - x.belief()*t - x.disbelief()*t),
          (2*x.disbelief()*t)/(1 - x.belief()*t - x.disbelief()*t)
        )
      })
    })

    new StageInit(
      context,
      directBetas,
      weightedWitnessBetas,
      stereotypeModel
    )
  }

  def computeRMSE(model: MlrModel, betas: Map[Provider,BetaDistribution]): Double = {
    val sqrdiff = betas.map(b => {
      val exp = b._2.expected()
      val row = 0d :: adverts(b._1)
      val query = convertRowToInstance(row, model.attVals, model.train)
      val pred = makePrediction(query, model)
      (exp-pred)*(exp-pred)
    }).sum
    1-Math.sqrt(sqrdiff / model.train.size.toDouble)
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
