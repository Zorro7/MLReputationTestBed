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
              val discountOpinions: Boolean = false,
              val witnessStereotypes: Boolean = true,
              val weightStereotypes: Boolean = true,
              override val explorationProbability: Double = 0.1
             ) extends CompositionStrategy with Exploration with BRSCore with StereotypeCore {

  val goodOpinionThreshold = 0.7
  val badOpinionThreshold = 0.3
  val prior = 0.5

  override val name: String =
    this.getClass.getSimpleName+"-"+baseLearner.getClass.getSimpleName +"-"+witnessWeight+
      (if (discountOpinions) "-discountOpinions" else "")+
      (if (witnessStereotypes) "-witnessStereotypes" else "")+
      (if (weightStereotypes) "-weightStereotypes" else "")

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[BurnettInit]

    val direct = init.directBetas.getOrElse(request.provider, new BetaDistribution(1,1)) // 1,1 for uniform
    val opinions = init.witnessBetas.values.map(
      _.getOrElse(request.provider, new BetaDistribution(0,0)) // 0,0 if the witness had no information about provider
    )

    val beta = getCombinedOpinions(direct, opinions, witnessWeight)

    val directPrior: Double = init.directStereotypeModel match {
      case Some(model) =>
        val row = makeTestRow(init, request)
        val query = convertRowToInstance(row, model.attVals, model.train)
        makePrediction(query, model)
      case None =>
        this.prior
    }

    val witnessStereotypes = init.witnessStereotypeModels.map(x => {
      val row = makeTestRow(init, request)
      val query = convertRowToInstance(row, x._2.attVals, x._2.train)
      makePrediction(query, x._2) * init.witnessStereotypeWeights.getOrElse(x._1, 1d)
    })
    val witnessPrior = witnessStereotypes.sum

    val witnessPriorWeight = if (weightStereotypes) init.witnessStereotypeWeights.values.sum else init.witnessStereotypeModels.size.toDouble

    val prior = (directPrior + witnessPrior) / (init.directStereotypeWeight + witnessPriorWeight)

    val score = beta.belief + prior * beta.uncertainty

    new TrustAssessment(init.context, request, score)
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {

    val directRecords: Seq[BootRecord] = getDirectRecords(network, context)
    val witnessRecords: Seq[BootRecord] = getWitnessRecords(network, context)

    val directBetas: Map[Provider,BetaDistribution] = makeDirectBetas(directRecords)
    val witnessBetas: Map[Client, Map[Provider, BetaDistribution]] = makeWitnessBetas(witnessRecords)

    val weightedWitnessBetas: Map[Client, Map[Provider, BetaDistribution]] =
      if (discountOpinions) weightWitnessBetas(witnessBetas, directBetas)
      else witnessBetas

    val directStereotypeModel: Option[MlrModel] =
      if (directRecords.isEmpty) None
      else Some(makeMlrsModel(directRecords, baseLearner, makeTrainRow))

    val witnessStereotypeModels: Map[Client,MlrModel] =
      if (witnessStereotypes) makeStereotypeModels(witnessRecords, baseLearner, makeTrainRow)
      else Map()

    val directStereotypeWeight: Double =
      if (weightStereotypes) directStereotypeModel match {
        case Some(x) => computeStereotypeWeight(x, directBetas)
        case None => 0d
      } else {
        1d
      }

    val witnessStereotypeWeights: Map[Client,Double] =
      if (weightStereotypes) {
        witnessStereotypeModels.map(sm => sm._1 ->
          computeStereotypeWeight(sm._2,  witnessBetas(sm._1))
        )
      } else {
        Map()
      }

    new BurnettInit(
      context,
      directBetas,
      weightedWitnessBetas,
      directStereotypeModel,
      witnessStereotypeModels,
      directStereotypeWeight,
      witnessStereotypeWeights
    )
  }

  def makeTrainRow(record: BootRecord): Seq[Any] = {
    record.rating :: adverts(record.service.request)
  }

  def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    0d :: adverts(request)
  }

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


}
