package jaspr.sellerssim.strategy

import jaspr.core.Network
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{StrategyInit, Exploration}
import jaspr.strategy.CompositionStrategy
import weka.classifiers.Classifier
import weka.classifiers.functions.LinearRegression

/**
 * Created by phil on 24/03/16.
 */
class Mlrs extends CompositionStrategy with Exploration with MlrsDirect with MlrsWitness {
  override val explorationProbability: Double = 0.1

  override val discreteClass: Boolean = false

  override def baseDirect: Classifier = new LinearRegression

  override def baseImputation: Classifier = new LinearRegression

  override def baseWitness: Classifier = new LinearRegression

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
