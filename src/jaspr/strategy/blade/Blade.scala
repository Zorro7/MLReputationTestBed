package jaspr.strategy.blade

import jaspr.core.Network
import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.{ClientContext, TrustAssessment, ServiceRequest}
import jaspr.core.strategy.{StrategyInit, Exploration}
import jaspr.strategy.{RatingStrategy, CompositionStrategy}
import jaspr.utilities.Dirichlet
import jaspr.utilities.matrix.{RowVector, Matrix}

/**
 * Created by phil on 25/03/16.
 */
class Blade(override val numBins: Int) extends CompositionStrategy with RatingStrategy with Exploration with BladeCore {

  override val explorationProbability: Double = 0.1

  override val lower: Double = -1d
  override val upper: Double = 1d

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val direct = toRatings(context.client.getProvenance(context.client))
    val witness = toRatings(network.gatherProvenance(context.client))

    val trustees: Seq[Provider] = direct.map(_.provider).distinct ++ witness.map(_.provider).distinct
    val witnesses: Seq[Client] = witness.map(_.client).distinct

    val dirPrior = this.getDirectPrior(context)
    val repPrior = this.getRepPrior(context.client)
    val dirModels = this.getDirectModels(direct, dirPrior)

    val repModels = this.getRepModels(witness, trustees, witnesses)

    val repMatrix: Map[(Client,Provider),Matrix] =
      (for (tr <- witnesses; te <- trustees) yield {
        val model = repModels.getOrElse((tr,te), repPrior)
        val opinionObs: RowVector = model.alpha @- repPrior.alpha
        (tr,te) -> dirModels.getOrElse(te, dirPrior).mean().transpose() @* opinionObs
      }).toMap

    new BladeInit(
      context,
      trustees, witnesses,
      dirPrior, repPrior,
      dirModels, repModels, repMatrix
    )
  }


  override def compute(init: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val directModels = init.asInstanceOf[BladeInit].directModels
    val repModels = init.asInstanceOf[BladeInit].repModels
    val trustees = init.asInstanceOf[BladeInit].trustees
    val witnesses = init.asInstanceOf[BladeInit].witnesses
    val dirModelPrior = init.asInstanceOf[BladeInit].dirModelPrior
    val repModelPrior = init.asInstanceOf[BladeInit].repModelPrior

    val repMatrix = init.asInstanceOf[BladeInit].repMatrix
    val trustee = request.provider

    val priorModel = directModels.getOrElse(trustee, dirModelPrior)

    val likelihoods = for (tr <- witnesses) yield {
      val cpt: Matrix = repMatrix
        .filter(x => x._1._1 == tr && x._1._2 != trustee).values
        .foldLeft(new Matrix(dirModelPrior.size,repModelPrior.size,1d))(_ @+ _)

      val cptnorm = cpt @/ cpt.colsum().sum

      val rcond: Matrix = divRcondRows(divMeanPrior(priorModel, cptDivRowSum(cptnorm)))

      val sourceAlpha: RowVector = repModels.getOrElse((tr,trustee), repModelPrior).alpha @- 1d

      (rcond @* sourceAlpha).rowsum().transpose() // total likelihood
    }

    val postModel = new Dirichlet(likelihoods.foldLeft(priorModel.alpha)(_ @+ _), priorModel.domain)

    new TrustAssessment(request, postModel.expval())
  }


}
