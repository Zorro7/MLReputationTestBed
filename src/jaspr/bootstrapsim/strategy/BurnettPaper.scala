package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.BootRecord
import jaspr.core.agent.{Client, Provider}
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.sellerssim.strategy.mlrs.MlrsCore
import jaspr.strategy.betareputation.BetaCore
import jaspr.strategy.{CompositionStrategy, RatingStrategy}
import jaspr.utilities.BetaDistribution
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.{AbstractClassifier, Classifier}

import scala.collection.mutable

/**
  * Created by phil on 30/09/2016.
  */
class BurnettPaper(baseLearner: Classifier,
                   override val numBins: Int,
                   val witnessWeight: Double = 0.5,
                   override val explorationProbability: Double = 0.1
             ) extends CompositionStrategy with Exploration with RatingStrategy with BetaCore with MlrsCore {

  class BurnettInit(context: ClientContext,
                    val stereotypeModel: Option[MlrsModel],
                    val directBetas: Map[Provider,BetaDistribution],
                    val witnessBetas: Map[Client,Map[Provider,BetaDistribution]],
                    val witnessRMSEs: Map[Client,Double],
                    val witnesses: Seq[Client]
                   ) extends StrategyInit(context)

  val stereotypeModel = new MultiRegression
  stereotypeModel.setSplitAttIndex(1)
  stereotypeModel.setClassifier(baseLearner)

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[BurnettInit]

    val directBeta = init.directBetas.get(request.provider) match {
      case None => new BetaDistribution(1,1)
      case Some(dist) => dist
    }

    val combinedBeta = getCombinedOpinions(directBeta, init.witnessBetas.values.flatMap(_.values))

    val stereotype = init.stereotypeModel match {
      case None => 0.5
      case Some(trustModel) =>
        val gatheredStereotypes = init.witnesses.map(witness => {
          val row = makeTestRow(init, witness, request)
          val query = convertRowToInstance(row, trustModel.attVals, trustModel.train)
          init.witnessRMSEs(witness) * makePrediction(query, trustModel)
        })
        gatheredStereotypes.sum / init.witnessRMSEs.values.sum
    }

    val score = combinedBeta.expected() + stereotype * combinedBeta.uncertainty()

    new TrustAssessment(init.context, request, score)
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords: Seq[BootRecord] =
      context.client.getProvenance[BootRecord](context.client)
    val witnessRecords: Seq[BootRecord] =
      if (witnessWeight == 0) Nil
      else network.gatherProvenance[BootRecord](context.client)
    val witnesses: Seq[Client] = witnessRecords.map(_.service.request.client).distinct

    if (directRecords.isEmpty && witnessRecords.isEmpty) {
      new BurnettInit(context, None, Map(), Map(), Map(), Nil)
    } else {

      val directBetas: Map[Provider,BetaDistribution] = directRecords.groupBy(
        x => x.service.request.provider
      ).mapValues(
        records => makeBetaDistribution(records.map(_.success))
      )

      val witnessProviderRecords: Map[Client,Seq[BootRecord]] = witnessRecords.groupBy(_.service.request.client)

      val witnessBetas: Map[Client,Map[Provider,BetaDistribution]] = witnessProviderRecords.mapValues(
        x => x.groupBy(
          _.service.request.provider
        ).mapValues(
          records => makeBetaDistribution(records.map(_.success))
        )
      )

      val witnesses: Seq[Client] = witnessRecords.map(_.service.request.client).distinct

      val rows: Iterable[Seq[Any]] =
        directBetas.map(x => makeTrainRow(context.client, x._1, x._2)) ++ //client, provider, beta
        witnessBetas.map(w => w._2.flatMap(x => makeTrainRow(w._1, x._1, x._2)).toList) //witness, provider, beta
      val directAttVals: Iterable[mutable.Map[Any, Double]] = List.fill(rows.head.size)(mutable.Map[Any, Double]())
      val doubleRows = convertRowsToDouble(rows, directAttVals)
      val atts = makeAtts(rows.head, directAttVals)
      val train = makeInstances(atts, doubleRows)
      val directModel = AbstractClassifier.makeCopy(baseLearner)
      directModel.buildClassifier(train)
      val stereotypeModel = new MlrsModel(directModel, train, directAttVals)

      val witnessRMSEs: Map[Client,Double] = witnesses.map(w => {
        val numRecords = witnessBetas(w).size
        val tops: Iterable[Double] = witnessBetas(w).map(p => {
          val row = makeTrainRow(w, p._1, p._2)
          val query = convertRowToInstance(row, stereotypeModel.attVals, stereotypeModel.train)
          val pred = makePrediction(query, stereotypeModel)
          val exp = p._2.expected()
          (pred - exp)*(pred-exp)
        })
        w -> (1 - Math.sqrt(tops.sum / numRecords))
      }).toMap


      new BurnettInit(
        context,
        Some(stereotypeModel),
        directBetas,
        witnessBetas,
        witnessRMSEs,
        witnesses
      )
    }
  }

  def makeTrainRow(witness: Client, provider: Provider, beta: BetaDistribution): Seq[Any] = {
    beta.expected() ::
      witness.name ::
      provider.generalAdverts.values.map(_.value.toString).toList
  }

  def makeTestRow(init: StrategyInit, pov: Client, request: ServiceRequest): Seq[Any] = {
    0d :: pov.name :: request.provider.generalAdverts.values.map(_.value.toString).toList
  }

}
