package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.BootRecord
import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.CompositionStrategy
import jaspr.strategy.mlr.MlrModel
import jaspr.utilities.BetaDistribution
import weka.classifiers.Classifier

/**
  * Created by phil on 07/10/16.
  */
class ContractStereotype(baseLearner: Classifier,
                        override val numBins: Int,
                        val witnessWeight: Double = 2d,
                        val discountOpinions: Boolean = false,
                        override val explorationProbability: Double = 0.1
                        ) extends CompositionStrategy with Exploration with BRSCore with StereotypeCore {


  override val prior: Double = 0.5
  override val goodOpinionThreshold: Double = 0.3
  override val badOpinionThreshold: Double = 0.7

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[ContractInit]

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
      makePrediction(query, x._2)
    })
    val witnessPrior = witnessStereotypes.sum

    val witnessPriorWeight = init.witnessStereotypeModels.size.toDouble

    val prior = (directPrior + witnessPrior) / (1d + witnessPriorWeight)

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

    val witnessStereotypeModels: Map[Client,MlrModel] = makeStereotypeModels(witnessRecords, baseLearner, makeTrainRow)

    new ContractInit(
      context,
      directBetas,
      weightedWitnessBetas,
      directStereotypeModel,
      witnessStereotypeModels
    )
  }

  def makeTrainRow(record: BootRecord): Seq[Any] = {
    record.rating :: adverts(record.service.request)
  }

  def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    0d :: adverts(request)
  }

}
