package jaspr.bootstrapsim.strategy

import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.strategy.StrategyInit
import jaspr.strategy.mlr.MlrModel
import jaspr.utilities.BetaDistribution

/**
  * Created by phil on 05/10/16.
  */
class BRSInit(context: ClientContext,
              val directBetas: Map[Provider,BetaDistribution],
              val witnessBetas: Map[Client,Map[Provider,BetaDistribution]]
             ) extends StrategyInit(context)


class BurnettInit(context: ClientContext,
                  directBetas: Map[Provider,BetaDistribution],
                  witnessBetas: Map[Client,Map[Provider,BetaDistribution]],
                  val directStereotypeModel: Option[MlrModel],
                  val witnessStereotypeModels: Map[Client,MlrModel],
                  val directStereotypeWeight: Double,
                  val witnessStereotypeWeights: Map[Client,Double],
                  val witnessStereotypeObs: Map[Client,Seq[Provider]]
                 ) extends BRSInit(context, directBetas, witnessBetas)

class StageInit(context: ClientContext,
                  directBetas: Map[Provider,BetaDistribution],
                  witnessBetas: Map[Client,Map[Provider,BetaDistribution]],
                  val stereotypeModel: MlrModel
               ) extends BRSInit(context, directBetas, witnessBetas)


class JasprStereotypeInit(context: ClientContext,
                          directBetas: Map[Provider,BetaDistribution],
                          witnessBetas: Map[Client,Map[Provider,BetaDistribution]],
                          val directStereotypeModel: Option[MlrModel],
                          val witnessStereotypeModels: Map[Client,MlrModel],
                          val directStereotypeWeight: Double,
                          val witnessStereotypeWeights: Map[Client,Double],
                          val witnessStereotypeObs: Map[Client,Seq[Provider]],
                          val translationModels: Map[Client,MlrModel]
                          ) extends BRSInit(context, directBetas, witnessBetas)


class PosstrInit(context: ClientContext,
                          directBetas: Map[Provider,BetaDistribution],
                          witnessBetas: Map[Client,Map[Provider,BetaDistribution]],
                          val directStereotypeModel: Option[MlrModel],
                          val witnessStereotypeModels: Map[Client,MlrModel],
                          val witnessStereotypeObs: Map[Client,Seq[Provider]],
                          val translationModels: Map[Client,MlrModel],
                          val ratingTranslationModels: Map[Client,MlrModel]
                         ) extends BRSInit(context, directBetas, witnessBetas)