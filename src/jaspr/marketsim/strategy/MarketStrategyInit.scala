package jaspr.marketsim.strategy

import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.ClientContext
import jaspr.core.strategy.StrategyInit
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