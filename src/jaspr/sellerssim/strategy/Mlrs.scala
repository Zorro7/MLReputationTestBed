package jaspr.sellerssim.strategy

import jaspr.core.Network
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{StrategyInit, Exploration}
import jaspr.strategy.CompositionStrategy
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.rules.OneR
import weka.classifiers.trees.{RandomForest, J48}
import weka.classifiers.{AbstractClassifier, Classifier}
import weka.classifiers.functions._

/**
 * Created by phil on 24/03/16.
 */
class Mlrs(val baseLearner: Classifier, override val numBins: Int) extends CompositionStrategy with Exploration with MlrsDirect with MlrsWitness {

  override val name = this.getClass.getSimpleName+"-"+baseLearner.getClass.getSimpleName+"-"+numBins

  override val explorationProbability: Double = 0.1

  override def baseDirect: Classifier = AbstractClassifier.makeCopy(baseLearner)

  override def baseImputation: Classifier = AbstractClassifier.makeCopy(baseLearner)
//  override def baseWitness: Classifier = AbstractClassifier.makeCopy(baseLearner)
  override val baseWitness = new MultiRegression
  baseWitness.setClassifier(AbstractClassifier.makeCopy(baseLearner))
  baseWitness.setSplitAttIndex(1)

  override def compute(init: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val directTA = super[MlrsDirect].compute(init, request)
    val witnessTA = super[MlrsWitness].compute(init, request)

    new TrustAssessment(init.context, request, directTA.trustValue + witnessTA.trustValue)
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    new MlrsInit(context,
      super[MlrsDirect].initStrategy(network, context).asInstanceOf[MlrsDirectInit],
      super[MlrsWitness].initStrategy(network, context).asInstanceOf[MlrsWitnessInit]
    )
  }


}
