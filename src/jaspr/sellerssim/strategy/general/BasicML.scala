package jaspr.sellerssim.strategy.general

import jaspr.core.Network
import jaspr.core.provenance.{RatingRecord, ServiceRecord, Record}
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.strategy.StrategyInit
import jaspr.sellerssim.service.ProductPayload

/**
 * Created by phil on 29/06/16.
 */
class BasicML extends SingleModelStrategy {

//
//  = {
//    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
//      record.service.request.provider.name :: // service identifier (client context)
//      Nil
//  }
//
//    = {
//    0d ::
//      request.provider.name ::
//      Nil
//  }
  override def getRecords(network: Network, context: ClientContext): Seq[Record] = {
    context.client.getProvenance(context.client)
  }

  override def makeTrainRow(baseRecord: Record): Seq[Any] = {
    val record = baseRecord.asInstanceOf[ServiceRecord with RatingRecord]
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.service.request.provider.name :: // service identifier (client context)
      Nil
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any]  = {
    0d ::
      request.provider.name ::
      Nil
  }
}
