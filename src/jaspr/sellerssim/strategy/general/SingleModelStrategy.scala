package jaspr.sellerssim.strategy.general

import jaspr.core.provenance.{RatingRecord, Record}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.CompositionStrategy
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.Chooser
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.Classifier
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.meta.FilteredClassifier
import weka.classifiers.trees.{J48, RandomForest}
import weka.filters.supervised.attribute.Discretize

/**
  * Created by phil on 29/06/16.
  */
trait SingleModelStrategy extends CompositionStrategy with Exploration with MlrCore {

  override val explorationProbability: Double = 0d

  val baseLearner: Classifier

  override val name: String = this.getClass.getSimpleName + (if (baseLearner != null) "-"+baseLearner.getClass.getSimpleName else "")

  baseLearner match {
    case x: NaiveBayes => x.setUseSupervisedDiscretization(true)
    case x: MultiRegression =>
      val bayes = new NaiveBayes
      bayes.setUseSupervisedDiscretization(true)
      x.setClassifier(bayes)
      x.setSplitAttIndex(-1)
    case x: J48 => x.setUnpruned(true)
    case x: RandomForest =>
      x.setNumFeatures(100)
    case _ => // do nothing
  }


  class BasicInit(context: ClientContext,
                  val trustModel: Option[MlrModel]
                 ) extends StrategyInit(context)

  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val records = getRecords(network, context)

    if (records.isEmpty) {
      new BasicInit(context, None)
    } else {
      val trustModel = makeMlrsModel(records, baseLearner, makeTrainRow)
      new BasicInit(context, Some(trustModel))
    }
  }

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init: BasicInit = baseInit.asInstanceOf[BasicInit]
    init.trustModel match {
      case None => new TrustAssessment(baseInit.context, request, Chooser.randomDouble(0,1))
      case Some(model) =>
        val row = makeTestRow(init, request)
        val query = convertRowToInstance(row, model.attVals, model.train)
        val result = makePrediction(query, model)
        new TrustAssessment(baseInit.context, request, result)
    }
  }

  def getRecords(network: Network, context: ClientContext): Seq[Record]

  def makeTrainRow(record: Record): Seq[Any]

  def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any]

}
