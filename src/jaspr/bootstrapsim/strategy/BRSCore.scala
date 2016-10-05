package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.BootRecord
import jaspr.utilities.BetaDistribution

/**
  * Created by phil on 05/10/16.
  */
trait BRSCore {

  def makeBetaDistribution(ratings: Iterable[Boolean]): BetaDistribution =
    new BetaDistribution(ratings.count(x => x) + 1d, ratings.count(x => !x) + 1d)

  /** combine all the ratings (interaction and weighted opinions) for final reputation value */
  def getCombinedOpinions(interactionTrust: BetaDistribution, opinions: Iterable[BetaDistribution]): BetaDistribution = {
    opinions.foldLeft(interactionTrust)((m1, m2) => // Start the reduce op with the client's trust value
      m1 + m2
    )
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
