package jaspr.sellerssim.strategy

import jaspr.core.Network
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{StrategyInit, Exploration}
import jaspr.strategy.CompositionStrategy
import jaspr.utilities.MultiRegression
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.rules.OneR
import weka.classifiers.trees.J48
import weka.classifiers.{AbstractClassifier, Classifier}
import weka.classifiers.functions.{SMOreg, LinearRegression}

/**
 * Created by phil on 24/03/16.
 */
class Mlrs extends CompositionStrategy with Exploration with MlrsDirect with MlrsWitness {
  override val explorationProbability: Double = 0.1

  override val numBins = 5
  override val discreteClass: Boolean = true
  val baseLearner = new OneR

  override def baseDirect: Classifier = AbstractClassifier.makeCopy(baseLearner)

  override def baseImputation: Classifier = AbstractClassifier.makeCopy(baseLearner)
  override val baseWitness = new MultiRegression
  baseWitness.setBase(AbstractClassifier.makeCopy(baseLearner))
  baseWitness.setSplitAttIndex(1)

  override def compute(init: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val directTA = super[MlrsDirect].compute(init, request)
    val witnessTA = super[MlrsWitness].compute(init, request)

    new TrustAssessment(request, directTA.trustValue + witnessTA.trustValue)
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    new MlrsInit(context,
      super[MlrsDirect].initStrategy(network, context).asInstanceOf[MlrsDirectInit],
      super[MlrsWitness].initStrategy(network, context).asInstanceOf[MlrsWitnessInit]
    )
  }

}
