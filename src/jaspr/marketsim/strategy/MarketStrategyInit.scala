package jaspr.marketsim.strategy

import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.ClientContext
import jaspr.core.strategy.StrategyInit
import jaspr.strategy.mlr.MlrModel
import jaspr.utilities.{Aggregate, BetaDistribution}

/**
  * Created by phil on 18/01/2017.
  */


class BRSInit(context: ClientContext,
              val directBetas: Map[Provider,BetaDistribution],
              val witnessBetas: Map[Client,Map[Provider,BetaDistribution]]
             ) extends StrategyInit(context)

class FireInit(context: ClientContext,
               val directAggregate: Map[Provider,Aggregate],
               val witnessAggregate: Map[Client,Map[Provider,Aggregate]]
              ) extends StrategyInit(context)

class BRSLikeInit(context: ClientContext,
                   val trustModel: Option[MlrModel]
                  ) extends StrategyInit(context)

class FireLikeInit(context: ClientContext,
                   val directModel: Option[MlrModel],
                   val witnessModels: Option[Map[Client,MlrModel]]
                  ) extends StrategyInit(context)

class BurnettInit(context: ClientContext,
                  directBetas: Map[Provider,BetaDistribution],
                  witnessBetas: Map[Client,Map[Provider,BetaDistribution]],
                  val directStereotypeModel: Option[MlrModel],
                  val witnessStereotypeModels: Map[Client,MlrModel],
                  val directStereotypeWeight: Double,
                  val witnessStereotypeWeights: Map[Client,Double]
                 ) extends BRSInit(context, directBetas, witnessBetas)