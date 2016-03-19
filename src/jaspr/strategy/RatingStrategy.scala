package jaspr.strategy

import jaspr.core.Network
import jaspr.core.provenance.{Record, RatingRecord, ServiceRecord}
import jaspr.core.service.ClientContext
import jaspr.core.strategy.{StrategyInit, Strategy}

/**
 * Created by phil on 19/03/16.
 */

class RatingStrategyInit(val directRecords: Seq[Rating],
                         val witnessRecords: Seq[Rating]
                           ) extends StrategyInit

abstract class RatingStrategy extends Strategy {

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val direct = toRatings(context.client.getProvenance(context.client))
    val witness = toRatings(network.gatherProvenance(context.client))
    new RatingStrategyInit(direct, witness)
  }

  def toRatings(records: Seq[Record]): Seq[Rating] = {
    records.map(x =>
      new Rating(
        x.asInstanceOf[ServiceRecord].service.request.provider,
        x.asInstanceOf[RatingRecord].rating
      )
    )
  }
}
