package jaspr.sellerssim.strategy.general

import jaspr.core.provenance.{RatingRecord, Record}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.sellerssim.strategy.mlrs.MlrsCore
import jaspr.strategy.CompositionStrategy
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.Classifier
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.meta.FilteredClassifier
import weka.classifiers.trees.{J48, RandomForest}
import weka.filters.supervised.attribute.Discretize

/**
  * Created by phil on 29/06/16.
  */
trait SingleModelStrategy extends CompositionStrategy with Exploration with MlrsCore {

  override val explorationProbability: Double = 0.1

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
                  val trustModel: Option[MlrsModel]
                 ) extends StrategyInit(context)

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val records = getRecords(network, context)

    if (records.isEmpty) {
      new BasicInit(context, None)
    } else {
//      val learner = new FilteredClassifier
//      learner.setClassifier(baseLearner)
//      val sd: Discretize = new Discretize
//      sd.setAttributeIndices("first-last")
//      learner.setFilter(sd)
      val trustModel = makeMlrsModel(records, baseLearner, makeTrainRow)
      val tmp = records.map(_.asInstanceOf[RatingRecord].rating)
//      println(tmp.count(_ > 0), tmp.size)
//      println(trustModel.train)
//      println(trustModel.model)
      new BasicInit(context, Some(trustModel))
    }
  }

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init: BasicInit = baseInit.asInstanceOf[BasicInit]
    init.trustModel match {
      case None => new TrustAssessment(baseInit.context, request, 0d)
      case Some(trustModel) =>
        val row = makeTestRow(init, request)
        val query = convertRowToInstance(row, trustModel.attVals, trustModel.train)

        val result = if (discreteClass && numBins <= 2) {
          val dist = trustModel.model.distributionForInstance(query)
          dist.zipWithIndex.map(x => x._1 * trustModel.train.classAttribute().value(x._2).toDouble).sum
        } else if (discreteClass) {
          val pred = trustModel.model.classifyInstance(query)
          trustModel.train.classAttribute().value(pred.toInt).toDouble
        } else trustModel.model.classifyInstance(query)

        new TrustAssessment(baseInit.context, request, result)
    }
  }

  def getRecords(network: Network, context: ClientContext): Seq[Record]

  def makeTrainRow(record: Record): Seq[Any]

  def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any]

}
