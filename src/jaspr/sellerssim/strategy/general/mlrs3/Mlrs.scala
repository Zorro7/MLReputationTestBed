package jaspr.sellerssim.strategy.general.mlrs3

import jaspr.core.agent.Provider
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.CompositionStrategy
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.{AbstractClassifier, Classifier}

/**
 * Created by phil on 24/03/16.
 */
class Mlrs(val baseLearner: Classifier,
           override val numBins: Int,
           val witnessWeight: Double = 0.5d,
           override val useAdvertProperties: Boolean = true
            ) extends CompositionStrategy with Exploration with MlrsDirect with MlrsWitness {


  override val name = this.getClass.getSimpleName+"-"+baseLearner.getClass.getSimpleName+"-"+numBins+"-"+witnessWeight

  override val explorationProbability: Double = 0.1

  override def baseDirect: Classifier = AbstractClassifier.makeCopy(baseLearner)

  override def baseImputation: Classifier = AbstractClassifier.makeCopy(baseLearner)
//  override def baseWitness: Classifier = AbstractClassifier.makeCopy(baseLearner)
  override val baseWitness = new MultiRegression
  baseWitness.setClassifier(AbstractClassifier.makeCopy(baseLearner))
  baseWitness.setSplitAttIndex(1)

  override def compute(init: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val directTA = super[MlrsDirect].compute(init, request)
    val witnessTA =
      if (witnessWeight <= 0) new TrustAssessment(init.context, request, 0d)
      else super[MlrsWitness].compute(init, request)

    new TrustAssessment(init.context, request, (1-witnessWeight)*directTA.trustValue + witnessWeight*witnessTA.trustValue)
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    new MlrsInit(context,
      super[MlrsDirect].initStrategy(network, context).asInstanceOf[MlrsDirectInit],
      if (witnessWeight != 0d) super[MlrsWitness].initStrategy(network, context).asInstanceOf[MlrsWitnessInit] else null
    )
  }

  def adverts(provider: Provider): List[Any] = {
    if (useAdvertProperties) provider.name :: provider.advertProperties.values.map(_.value).toList
    else provider.name :: Nil
  }

}
