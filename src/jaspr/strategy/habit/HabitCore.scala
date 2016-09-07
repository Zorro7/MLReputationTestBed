package jaspr.strategy.habit

import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.ClientContext
import jaspr.core.strategy.StrategyInit
import jaspr.strategy.Rating
import jaspr.utilities.matrix.RowVector
import jaspr.utilities.{Dirichlet, Discretization}

/**
 * Created by phil on 24/03/16.
 */
trait HabitCore extends Discretization {

  class HabitInit(context: ClientContext,
                          val trustees: Seq[Provider],
                          val priorDist: Dirichlet,
                          val directObs: Map[Provider, RowVector],
                          val repModels: Map[(Client,Provider), Dirichlet]
                           ) extends StrategyInit(context)


  def getDirectPrior(client: ClientContext) = {
    new Dirichlet(numBins)
  }

  def getDirectObs(directRatings: Seq[Rating]): Map[Provider,RowVector] = {
    directRatings.groupBy(_.provider).mapValues(_.map(x => discretizeDouble(x.rating)))
  }

  def getRepModels(witnessReports: Seq[Rating],
                   trustees: Seq[Provider],
                   witnesses: Seq[Client]): Map[(Client,Provider),Dirichlet] = {
    (for (w <- witnesses; t <- trustees) yield {
      (w,t) -> new Dirichlet(numBins, 1).observe(
        witnessReports.withFilter(x => x.client == w && x.provider == t).map(x => discretizeDouble(x.rating))
      )
    }).toMap
  }

}
