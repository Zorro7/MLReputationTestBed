package jaspr.bootstrapsim.strategy

import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.ClientContext
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
                  val stereotypeModels: Map[Client,MlrModel],
                  val RMSEs: Map[Client,Double]
                 ) extends BRSInit(context, directBetas, witnessBetas)
