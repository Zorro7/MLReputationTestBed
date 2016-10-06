package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.BootRecord
import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.ClientContext
import jaspr.core.simulation.Network
import jaspr.utilities.BetaDistribution

/**
  * Created by phil on 05/10/16.
  */
trait BRSCore {

  val prior: Double
  val witnessWeight: Double
  val goodOpinionThreshold: Double
  val badOpinionThreshold: Double

  def getDirectRecords(network: Network, context: ClientContext): Seq[BootRecord] = {
    context.client.getProvenance[BootRecord](context.client)
  }

  def getWitnessRecords(network: Network, context: ClientContext): Seq[BootRecord] = {
    if (witnessWeight == 0) Nil
    else network.gatherProvenance[BootRecord](context.client)
  }

  def makeDirectBetas(directRecords: Seq[BootRecord]): Map[Provider,BetaDistribution] = {
    if (witnessWeight != 1) makeOpinions(directRecords, r => r.service.request.provider)
    else Map()
  }

  def makeWitnessBetas(witnessRecords: Seq[BootRecord]): Map[Client, Map[Provider, BetaDistribution]] = {
      if (witnessWeight > 0) makeOpinions(witnessRecords, r => r.service.request.client, r => r.service.request.provider)
      else Map()
  }

  private def makeWitnessWeightings(witnessBetas: Map[Client, Map[Provider, BetaDistribution]],
                            directBetas: Map[Provider, BetaDistribution]
                          ): Map[Client, Double] = {
    witnessBetas.map(wb => {
      wb._1 -> wb._2.map(x => {
        val directOpinion = directBetas.getOrElse(x._1, new BetaDistribution(0, 0))
        if (x._2.belief > goodOpinionThreshold) directOpinion
        else if (x._2.belief < badOpinionThreshold) new BetaDistribution(directOpinion.beta, directOpinion.alpha) //swap the alphas for agreement with witnessOpinion
        else new BetaDistribution(0,0)
      }).foldLeft(new BetaDistribution)(_ + _)
    }).mapValues(_.belief())
  }

  private def applyWitnessWeightings(witnessBetas: Map[Client, Map[Provider, BetaDistribution]],
                             witnessWeightings: Map[Client, Double]
                            ): Map[Client, Map[Provider, BetaDistribution]] = {
    witnessBetas.map(wb => wb._1 -> {
      val t = witnessWeightings.getOrElse(wb._1, 1d)
      wb._2.mapValues(x => {
        new BetaDistribution(
          (2 * x.belief() * t) / (1 - x.belief() * t - x.disbelief() * t),
          (2 * x.disbelief() * t) / (1 - x.belief() * t - x.disbelief() * t)
        )
      })
    })
  }

  def weightWitnessBetas(witnessBetas: Map[Client, Map[Provider, BetaDistribution]],
                         directBetas: Map[Provider, BetaDistribution]) = {
    applyWitnessWeightings(witnessBetas, makeWitnessWeightings(witnessBetas, directBetas))
  }

  def makeBetaDistribution(ratings: Iterable[Boolean]): BetaDistribution =
    new BetaDistribution(ratings.count(x => x) + 1d, ratings.count(x => !x) + 1d)

  def getCombinedOpinions(direct: BetaDistribution,
                          opinions: Iterable[BetaDistribution],
                          witnessWeight: Double): BetaDistribution = {
    if (witnessWeight == 0) direct
    else if (witnessWeight == 1) opinions.foldLeft(new BetaDistribution())(_ + _)
    else if (witnessWeight == 2) opinions.foldLeft(direct)(_ + _)
    else getCombinedOpinions(direct * (1-witnessWeight), opinions.map(_ * witnessWeight), witnessWeight = 2)
  }

  def makeOpinions[K](records: Iterable[BootRecord], grouping: BootRecord => K): Map[K,BetaDistribution] = {
    records.groupBy(
      grouping
    ).mapValues(
      rs => makeBetaDistribution(rs.map(_.success))
    )
  }

  def makeOpinions[K1,K2](records: Iterable[BootRecord],
                          grouping1: BootRecord => K1,
                          grouping2: BootRecord => K2): Map[K1,Map[K2,BetaDistribution]] = {
    records.groupBy(
      grouping1
    ).mapValues(
      rs => makeOpinions(rs, grouping2)
    )
  }
}
