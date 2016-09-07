package jaspr.strategy

import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.ClientContext
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Strategy, StrategyInit}

/**
 * Created by phil on 19/03/16.
 */

class RatingStrategyInit(context: ClientContext,
                         val directRecords: Seq[Rating],
                         val witnessRecords: Seq[Rating]
                           ) extends StrategyInit(context)

trait RatingStrategy extends Strategy {

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val direct = toRatings(context.client.getProvenance(context.client))
    val witness = toRatings(network.gatherProvenance(context.client))
    new RatingStrategyInit(context, direct, witness)
  }

  def toRatings(records: Seq[ServiceRecord with RatingRecord]): Seq[Rating] = {
    records.map(x =>
      new Rating(
        x.service.request.client,
        x.service.request.provider,
        x.service.end,
        x.rating
      )
    )
  }
}
