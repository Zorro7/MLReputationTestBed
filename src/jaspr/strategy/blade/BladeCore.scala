package jaspr.strategy.blade

import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.ClientContext
import jaspr.core.strategy.StrategyInit
import jaspr.strategy.Rating
import jaspr.utilities.matrix.Matrix
import jaspr.utilities.{Dirichlet, Discretization}

/**
  * Created by phil on 01/11/15.
  */
trait BladeCore extends Discretization {


  class BladeInit(context: ClientContext,
                  val trustees: Seq[Provider],
                  val witnesses: Seq[Client],
                  val dirModelPrior: Dirichlet,
                  val repModelPrior: Dirichlet,
                  val directModels: Map[Provider, Dirichlet],
                  val repModels: Map[(Client, Provider), Dirichlet],
                  val repMatrix: Map[(Client, Provider), Matrix]
                 ) extends StrategyInit(context)


  def getRepPrior(client: Client): Dirichlet = {
    new Dirichlet(numBins)
  }

  def getDirectPrior(context: ClientContext): Dirichlet = {
    new Dirichlet(numBins)
  }

  def getDirectModels(directRatings: Seq[Rating], prior: Dirichlet): Map[Provider, Dirichlet] = {
    //    val provratings: Map[AbstractAgent,List[Rating]] = directRatings.groupBy(_.provider)
    //    val asdf: Map[AbstractAgent, Map[Double, Int]] = provratings.mapValues(x => x.groupBy(y => discretize(y.rating)).mapValues(_.size+1))
    //    val out = asdf.mapValues(x => new Dirichlet((0 until numbins).map(y => x.getOrElse(y, 1).toDouble)))
    directRatings.groupBy(_.provider).mapValues(x => prior.observe(x.map(y => discretizeDouble(y.rating))))
  }

  def getRepModels(witnessReports: Seq[Rating],
                   trustees: Seq[Provider],
                   witnesses: Seq[Client]): Map[(Client, Provider), Dirichlet] = {
    (for (w <- witnesses; t <- trustees) yield {
      val r = witnessReports.filter(x => x.client == w && x.provider == t)
      (w, t) -> getRepPrior(w).observe(
        witnessReports.filter(x => x.client == w && x.provider == t).map(x => discretizeDouble(x.rating))
      )
    }).toMap
  }

  def cptDivRowSum(cpt: Matrix) = cpt @/ cpt.rowsum()

  def divMeanPrior(priorModel: Dirichlet, rcond: Matrix) = rcond @* priorModel.mean().transpose()

  def divRcondRows(rcond: Matrix) = rcond @/ rcond.colsum()


}
