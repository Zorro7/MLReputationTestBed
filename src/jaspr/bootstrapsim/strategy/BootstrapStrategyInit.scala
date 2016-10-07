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
                  val directStereotypeModel: Option[MlrModel],
                  val witnessStereotypeModels: Map[Client,MlrModel],
                  val directStereotypeWeight: Double,
                  val witnessStereotypeWeights: Map[Client,Double]
                 ) extends BRSInit(context, directBetas, witnessBetas)

class StageInit(context: ClientContext,
                  directBetas: Map[Provider,BetaDistribution],
                  witnessBetas: Map[Client,Map[Provider,BetaDistribution]],
                  val stereotypeModel: MlrModel
               ) extends BRSInit(context, directBetas, witnessBetas)

class ContractInit(context: ClientContext,
                  directBetas: Map[Provider,BetaDistribution],
                  witnessBetas: Map[Client,Map[Provider,BetaDistribution]],
                  val directStereotypeModel: Option[MlrModel],
                  val witnessStereotypeModels: Map[Client,MlrModel]
                 ) extends BRSInit(context, directBetas, witnessBetas)