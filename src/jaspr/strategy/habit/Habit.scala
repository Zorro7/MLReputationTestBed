package jaspr.strategy.habit

import jaspr.core.Network
import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.{TrustAssessment, ServiceRequest, ClientContext}
import jaspr.core.strategy.{StrategyInit, Exploration}
import jaspr.strategy.{RatingStrategy, CompositionStrategy}
import jaspr.utilities.Dirichlet
import jaspr.utilities.matrix.RowVector

/**
 * Created by phil on 24/03/16.
 */
class Habit(override val numBins: Int) extends CompositionStrategy with RatingStrategy with Exploration with HabitCore {

  override val name: String = this.getClass.getSimpleName+"-"+numBins

  override val explorationProbability: Double = 0.1

  override val lower: Double = -1d
  override val upper: Double = 1d


  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val direct = toRatings(context.client.getProvenance(context.client))
    val witness = toRatings(network.gatherProvenance(context.client))

    val trustees: Seq[Provider] = direct.map(_.provider).distinct ++ witness.map(_.provider).distinct
    val witnesses: Seq[Client] = witness.map(_.client).distinct

    new HabitInit(
      context,
      trustees,
      getDirectPrior(context),
      getDirectObs(direct),
      getRepModels(witness, trustees, witnesses)
    )
  }

  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[HabitInit]
    val directObs = init.directObs
    val repModels = init.repModels
    val trustees = init.trustees
    val priorDist = init.priorDist

    val trustee = request.provider

    if (directObs.isEmpty && repModels.isEmpty) return new TrustAssessment(request, 0d)

    // Generate mixture components by observing the direct observations for each trustee.
    val directComponent: Seq[Dirichlet] = trustees.map(x =>
      priorDist.observe(directObs.getOrElse(x, Seq()))
    )

    // Initialise the component weights to account for the marginal likelihood of the direct observations according to each component.
    val directWeights: RowVector = (trustees zip directComponent).map(x =>
      if (x._1 == trustee) priorDist.marginalLogLikelihood(directObs.getOrElse(trustee, Seq()))
      else x._2.marginalLogLikelihood(directObs.getOrElse(trustee, Seq()))
    )

    // Observe direct observations of this trustee for components containing information about other trustees.
    val mixComponents: Seq[Dirichlet] = (trustees zip directComponent).map(x =>
      if (x._1 == trustee) x._2
      else x._2.observe(directObs.getOrElse(trustee,Seq()))
    )


    // Now calculate the log weights based on each reported opinion distribution, and sum them for each component
    val repWeights: Map[Provider, Double] = (for (((w,te),model) <- repModels) yield {
      if (te == trustee) (w,trustee) -> model.logweight(priorDist)
      else (w,te) -> model.logweight(repModels.getOrElse((w, trustee), priorDist))
    }).groupBy(x => x._1._2).mapValues(x => x.values.sum)

    val weights: RowVector = (trustees zip directWeights).map(x => x._2 + repWeights.getOrElse(x._1, 0d))

    def minusMax(weights: RowVector) = weights @- weights.max
    def minusLogSumExp(weights: RowVector) = weights @- weights.exp.log.sum
    def expDivSumExp(weights: RowVector) = weights.exp @/ weights.exp.sum

    val normWeights = expDivSumExp(minusLogSumExp(minusMax(weights)))


    // Calculate the expected utility and standard error according to each individual component.
    val expval = (normWeights @* mixComponents.map(x => x.expval())).sum
    //    val stderr = (normWeights @* mixComponents.map(x => x.stderr())).sum

    new TrustAssessment(request, expval)
  }

}
