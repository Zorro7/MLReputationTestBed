package jaspr.strategy.betareputation

import jaspr.core.agent.{Client, Agent}
import jaspr.strategy.Rating
import jaspr.utilities.BetaDistribution

/**
 * Created by phil on 19/03/16.
 */
trait BetaCore {
  def makeBetaDistribution(ratings: Iterable[Boolean]): BetaDistribution =
    new BetaDistribution(ratings.count(x => x) + 1d, ratings.count(x => !x) + 1d)

  def makeWitnessBetaDistribution(ratings: Iterable[Rating]): Map[Client, BetaDistribution] = {
    ratings.groupBy(x =>
      x.client // group by witness agent
    ).mapValues[BetaDistribution](x =>
      makeBetaDistribution(x.map(y => y.success))
    )
  }

  /** combine all the ratings (interaction and weighted opinions) for final reputation value */
  def getCombinedOpinions(interactionTrust: BetaDistribution, opinions: Iterable[BetaDistribution]): BetaDistribution = {
    opinions.map(x => x).foldLeft(interactionTrust)((m1, m2) => // Start the reduce op with the client's trust value
      m1 + m2
    )
  }
}
