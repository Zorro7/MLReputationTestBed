package jaspr.marketsim.strategy

import jaspr.core.agent.{Client, Provider}
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.StrategyInit
import jaspr.marketsim.agent.MarketRecord
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.BetaDistribution
import weka.classifiers.trees.M5P
import weka.classifiers.{AbstractClassifier, Classifier}

import scala.collection.mutable


/**
  * Created by phil on 19/01/17.
  */
class Burnett(_witnessWeight: Double = 2d,
              val witnessStereotypes: Boolean = true
             ) extends StrategyCore with BRSCore with MlrCore {

  val baseLearner: Classifier = new M5P
  override val numBins: Int = 0
  override val lower: Double = 0
  override val upper: Double = 0

  val weightStereotypes: Boolean = false
  val ratingStereotype: Boolean = false

  override val prior: Double = 0.5
  override val witnessWeight: Double = _witnessWeight


  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[BurnettInit]

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
      val row = stereotypeTestRow(init, request)
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
    val directRecords: Seq[ServiceRecord with RatingRecord] = getDirectRecords(network, context)
    val witnessRecords: Seq[ServiceRecord with RatingRecord] = getWitnessRecords(network, context)

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

    val groupedWitnessRecords: Map[Client,Seq[ServiceRecord with RatingRecord]] = witnessRecords.groupBy(_.service.request.client)

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
      witnessStereotypeWeights
    )
  }

  def stereotypeTrainRow(record: ServiceRecord with RatingRecord, labels: Map[Provider,Double]): Seq[Any] = {
    labels.getOrElse(record.provider, record.rating) :: adverts(record.service.request)
  }

  def stereotypeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    0d :: adverts(request)
  }

  def adverts(provider: Provider): List[Any] = {
    provider.generalAdverts.values.map(_.value.toString).toList
  }

  def adverts(request: ServiceRequest): List[Any] = {
    request.provider.generalAdverts.values.map(_.value.toString).toList
  }



  def makeStereotypeModel(records: Seq[ServiceRecord with RatingRecord],
                          labels: Map[Provider,Double],
                          baseLearner: Classifier,
                          makeTrainRow: (ServiceRecord with RatingRecord,Map[Provider,Double]) => Seq[Any]
                         ): MlrModel = {
    val stereotypeObs: Seq[ServiceRecord with RatingRecord] =
      if (ratingStereotype) records
      else distinctBy[ServiceRecord with RatingRecord,Provider](records, _.provider)  // Get the distinct records cause here we assume observations are static for each truster/trustee pair.
    makeMlrsModel[ServiceRecord with RatingRecord](stereotypeObs, baseLearner, makeTrainRow(_: ServiceRecord with RatingRecord, labels))
  }

  override def makeMlrsModel[T <: Record](records: Seq[T], baseModel: Classifier,
                                          makeTrainRow: T => Seq[Any],
                                          makeWeight: T => Double = null): MlrModel = {
    val rows = records.map(makeTrainRow)
    val weights = if (makeWeight == null) Nil else records.map(makeWeight)
    val directAttVals: Iterable[mutable.Map[Any, Double]] = List.fill(rows.head.size)(mutable.Map[Any, Double]("true" -> 1.0, "false" -> 0.0))
    val doubleRows = convertRowsToDouble(rows, directAttVals)
    val atts = makeAtts(rows.head, directAttVals)
    val train = makeInstances(atts, doubleRows, weights)
    val directModel = AbstractClassifier.makeCopy(baseModel)
    directModel.buildClassifier(train)
    new MlrModel(directModel, train, directAttVals)
  }

  def distinctBy[T,P](xs: Iterable[T], f: T => P) = {
    xs.foldRight((List[T](), Set[P]())) {
      case (o, cum@(objects, props)) =>
        if (props(f(o))) cum else (o :: objects, props + f(o))
    }._1
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
