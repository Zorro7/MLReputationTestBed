package jaspr.strategy.habit

import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, StrategyInit}
import jaspr.strategy.{CompositionStrategy, RatingStrategy}
import jaspr.utilities.Dirichlet
import jaspr.utilities.matrix.RowVector

/**
  * Created by phil on 24/03/16.
  */
class Habit(override val numBins: Int) extends CompositionStrategy with RatingStrategy with Exploration with HabitCore {

  override val name: String = this.getClass.getSimpleName + "-" + numBins

  override val explorationProbability: Double = 0.1

  override val lower: Double = -1d
  override val upper: Double = 1d


  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val direct = toRatings(context.client.getProvenance(context.client))
    val witness = toRatings(network.gatherProvenance(context.client))

    val trustees: Seq[Provider] = (direct.map(_.provider).distinct ++ witness.map(_.provider).distinct).distinct.sortBy(_.id)
    val witnesses: Seq[Client] = witness.map(_.client).distinct

    new HabitInit(
      context,
      witnesses,trustees,
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



    val numProviders = trustees.size
    val numWitnesses = init.witnesses.size
    val flatObs = directObs.groupBy(_._1).flatMap(_._2)


    val trustee = request.provider

    if (directObs.isEmpty && repModels.isEmpty) return new TrustAssessment(baseInit.context, request, 0d)

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
      else x._2.observe(directObs.getOrElse(trustee, Seq()))
    )

    // Now calculate the log weights based on each reported opinion distribution, and sum them for each component
    val repWeights: Map[Provider, Double] = (for (((w, te), model) <- repModels) yield {
      if (te == trustee) (w, trustee) -> model.logweight(priorDist)
      else (w, te) -> model.logweight(repModels.getOrElse((w, trustee), priorDist))
    }).groupBy(x => x._1._2).mapValues(x => x.values.sum)

    val weights: RowVector = (trustees zip directWeights).map(x => x._2 + repWeights.getOrElse(x._1, 0d))

    //weights = unNormalisedLogWeights - max(unNormalisedLogWeights);
    def minusMax(weights: RowVector) = weights @- weights.max
    //weights = weights - log(sum(exp(weights)));
    def minusLogSumExp(weights: RowVector) = weights @- Math.log(weights.exp.sum)
    def minusLogSumExp_broken(weights: RowVector) = weights @- weights.exp.log.sum
    //weights = exp(weights) ./ sum(exp(weights)); % too be sure
    def expDivSumExp(weights: RowVector) = weights.exp @/ weights.exp.sum



//    val testWeights: RowVector = new RowVector(("-72.08808254870051,-65.11337316802944,-65.86350727996299,-65.13399245523216,-65.40105524048121,-64.89022961671523," +
//      "-65.30389149202757,-65.00046106260764,-87.56845530470514,-65.40105524048121,-72.34425979036584,-82.23844365678204,-65.40105524048121," +
//      "-65.19853097636974,-65.4906673991709,-65.64481807899817,-75.71812150279153,-65.13399245523217,-85.43819721002393,-85.85430298081205," +
//      "-88.5201340020295,-65.4906673991709,-65.64481807899816,-65.44984540465065,-70.52691618494013,-81.96142475942409,-65.2675238478567," +
//      "-80.7999772665959,-65.77834947162269,-65.51128668637365,-65.42167452768396,-78.65872087424268,-83.56218063568808,-65.55520592030848," +
//      "-69.71492216343857,-82.44715588810979,-65.55520592030848,-85.95485915330612,-65.20298532671913,-65.4906673991709,-65.13399245523216," +
//      "-65.20298532671912,-65.77834947162269,-65.28814313505943,-87.65894276250557,-85.8268937216597,-65.28814313505943,-85.06442003669221," +
//      "-65.13399245523217,-81.39528050786934,-69.78668215718824,-74.2708161585162,-84.67915456400314,-65.4906673991709,-66.18381457973084," +
//      "-65.13399245523217,-86.58526198439851,-65.66543736620089,-65.22360461392186,-65.28814313505941,-65.35713600654638,-70.27412137148879," +
//      "-65.33651671934363,-65.18236603951638,-82.48119098741829,-76.63557570535295,-68.20345951746098,-89.87158760566291,-65.64481807899817," +
//      "-82.67167740788697,-65.33206236899427,-65.13399245523217,-71.9537914420465,-65.33651671934365,-65.64481807899816,-82.9292916726113," +
//      "-83.20046766317702,-65.4906673991709,-81.0314638337545,-73.12919767351299,-85.82200719373931,-81.00043308318538,-65.35713600654637," +
//      "-71.12241873220907,-84.81177599092634,-65.49066739917092,-84.52008236767149,-65.51128668637365,-65.55520592030847,-84.67914243371114," +
//      "-65.35713600654637,-80.9810663413732,-65.62419879179542,-65.82268528544274,-65.55520592030847,-69.11952165178015,-65.04438029654247," +
//      "-65.51128668637364,-87.65316857423771,-71.58313576306925").split(",").map(_.toDouble))
//
//    println(testWeights)
//    println(minusMax(testWeights))
//    println(minusLogSumExp(minusMax(testWeights)))
//    println(expDivSumExp(minusLogSumExp(minusMax(testWeights))))

//    println("--")
    val normWeights = expDivSumExp(minusLogSumExp(minusMax(weights)))
    val normWeights_broken = expDivSumExp(minusLogSumExp_broken(minusMax(weights)))

    // Calculate the expected utility and standard error according to each individual component.
    val expval = (normWeights @* mixComponents.map(x => x.expval())).sum
    val expval_broken = (normWeights_broken @* mixComponents.map(x => x.expval())).sum
    //    val stderr = (normWeights @* mixComponents.map(x => x.stderr())).sum


    if (expval.isNaN && trustees.contains(request.provider)) {
//      println(trustees)

//      println("directWeights = "+directWeights)

//      println("mixComponents = "+mixComponents)

//      println("repWeights = "+ repWeights)

      println("weights = "+ weights)

      println("normWeights = "+ normWeights)
      println("normWeights_broken = "+ normWeights_broken)

      var str = "evalTrustee = "+(trustees.indexOf(request.provider)+1)+"\n"
      str += trustees.map(p =>
        flatObs.getOrElse(p, "[]")
      ).mkString("observations(1,1:" + numProviders + ") = {", ",", "}\n")
      str += "observations(2:" + numWitnesses + "+1,1:" + numProviders + ") = ...\n{"
      init.witnesses.foreach(c => {
        str += trustees.map(p =>
          repModels.getOrElse((c, p), "[1,1],[0,1]")
        ).mkString("\tdirichlet(", "),dirichlet(", "); ...\n")
      })
      str += "}\n"
      println(str)

      println("trustValue = "+expval)
      println("----")
    }


    new TrustAssessment(baseInit.context, request, expval)
  }

}
