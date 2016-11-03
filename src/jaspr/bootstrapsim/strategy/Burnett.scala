package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.{BootRecord, Observations, Trustee, Truster}
import jaspr.core.agent.{Client, Provider}
import jaspr.core.provenance.{RatingRecord, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.betareputation.BetaCore
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.strategy.{CompositionStrategy, RatingStrategy}
import jaspr.utilities.BetaDistribution
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.Classifier

/**
  * Created by phil on 30/09/2016.
  */
class Burnett(baseLearner: Classifier,
              override val numBins: Int,
              _witnessWeight: Double = 2d,
              val witnessStereotypes: Boolean = true,
              val subjectiveStereotypes: Boolean = false,
              val hideTrusteeIDs: Boolean = false,
              override val limitedObservations: Boolean = false
             ) extends CompositionStrategy with Exploration with BRSCore with StereotypeCore {


  override val explorationProbability: Double = 0d
  val discountOpinions: Boolean = false
  val weightStereotypes: Boolean = false
  val ratingStereotype: Boolean = false

  // If the trustee ids can't be broadcast, witnesses can't offer their witness-reputation assessments.
  override val witnessWeight: Double = if (hideTrusteeIDs) 0d else _witnessWeight

  val goodOpinionThreshold = 0.7
  val badOpinionThreshold = 0.3
  val prior = 0.5

  override val name: String =
    this.getClass.getSimpleName+"-"+baseLearner.getClass.getSimpleName +"-"+witnessWeight+"-"+explorationProbability+
      (if (discountOpinions) "-discountOpinions" else "")+
      (if (witnessStereotypes) "-witnessStereotypes" else "")+
      (if (weightStereotypes) "-weightStereotypes" else "")+
      (if (ratingStereotype) "-ratingStereotype" else "")+
      (if (subjectiveStereotypes) "-subjectiveStereotypes" else "")+
      (if (hideTrusteeIDs) "-hideTrusteeIDs" else "")+
      (if (limitedObservations) "-limitedObservations" else "")

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[BurnettInit with Observations]

    val direct = init.directBetas.getOrElse(request.provider, new BetaDistribution(1,1)) // 1,1 for uniform
    val opinions = init.witnessBetas.values.map(
      _.getOrElse(request.provider, new BetaDistribution(0,0)) // 0,0 if the witness had no information about provider
    )

    val beta = getCombinedOpinions(direct, opinions, witnessWeight)

    val directPrior: Double = init.directStereotypeModel match {
      case Some(model) =>
        val row = stereotypeTestRow(init, request)
        val query = convertRowToInstance(row, model.attVals, model.train)
        makePrediction(query, model)
      case None =>
        this.prior
    }

    val witnessStereotypes = init.witnessStereotypeModels.map(x => {
      val row =
        if (subjectiveStereotypes && // doing subjectivity?
          (hideTrusteeIDs || //can't broadcast trustee ids?
          !init.witnessStereotypeObs.getOrElse(x._1, Nil).contains(request.provider) //witness never seen trustee?
            )) {
          stereotypeTestRow(init, request) //use truster-observed stereotype
        } else {
          0 :: objectiveStereotypeRow(x._1, request.provider) //use witness-observed stereotype
        }
      val query = convertRowToInstance(row, x._2.attVals, x._2.train)
      val res = makePrediction(query, x._2)
      res * init.witnessStereotypeWeights.getOrElse(x._1, 1d)
    })
    val witnessPrior = witnessStereotypes.sum

    val witnessPriorWeight = if (weightStereotypes) init.witnessStereotypeWeights.values.sum else init.witnessStereotypeModels.size.toDouble

    val prior = (directPrior + witnessPrior) / (init.directStereotypeWeight + witnessPriorWeight)

    val score = beta.belief + prior * beta.uncertainty

    new TrustAssessment(init.context, request, score)
  }


  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {

    val directRecords: Seq[BootRecord] = getDirectRecords(network, context)
    val witnessRecords: Seq[BootRecord] = getWitnessRecords(network, context)

    val directBetas: Map[Provider,BetaDistribution] = makeDirectBetas(directRecords)
    val witnessBetas: Map[Client, Map[Provider, BetaDistribution]] = makeWitnessBetas(witnessRecords)

    val weightedWitnessBetas: Map[Client, Map[Provider, BetaDistribution]] =
      if (discountOpinions) weightWitnessBetas(witnessBetas, directBetas)
      else witnessBetas

    val directStereotypeModel: Option[MlrModel] =
      if (directRecords.isEmpty) None
      else {
        val labels =
          if (ratingStereotype) Map[Provider,Double]()
          else directBetas.mapValues(x => x.belief + prior * x.uncertainty)
        Some(makeStereotypeModel(directRecords,labels,baseLearner,stereotypeTrainRow))
      }

    val groupedWitnessRecords: Map[Client,Seq[BootRecord]] = witnessRecords.groupBy(_.service.request.client)

    val witnessStereotypeObs: Map[Client,Seq[Provider]] =
      groupedWitnessRecords.mapValues(_.flatMap(_.observations.keys))

    val witnessStereotypeModels: Map[Client,MlrModel] =
      if (witnessStereotypes) {
        groupedWitnessRecords.map(wr => wr._1 -> {
          val labels =
            if (ratingStereotype) Map[Provider,Double]()
            else witnessBetas.getOrElse(wr._1, Map()).mapValues(x => x.belief + prior * x.uncertainty)
          makeStereotypeModel(wr._2,labels,baseLearner,stereotypeTrainRow)
        })
      }
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
      witnessStereotypeWeights,
      witnessStereotypeObs
    ) with Observations {
      override val possibleRequests: Seq[ServiceRequest] =
        if (limitedObservations) Nil
        else requests
    }
  }

  def stereotypeTrainRow(record: BootRecord, labels: Map[Provider,Double]): Seq[Any] = {
    labels.getOrElse(record.trustee, record.rating) :: adverts(record.service.request)
  }

  def stereotypeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    0d :: adverts(request)
  }

}
