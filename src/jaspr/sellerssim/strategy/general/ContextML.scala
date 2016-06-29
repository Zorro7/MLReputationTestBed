package jaspr.sellerssim.strategy.general

import jaspr.core.Network
import jaspr.core.provenance.{RatingRecord, ServiceRecord, Record}
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.strategy.StrategyInit
import jaspr.sellerssim.service.ProductPayload

/**
 * Created by phil on 29/06/16.
 */
trait ContextML extends SingleModelStrategy {

  val baseStrategy: SingleModelStrategy

  override def getRecords(network: Network, context: ClientContext): Seq[Record] = {
    baseStrategy.getRecords(network, context)
  }

  override def makeTrainRow(baseRecord: Record): Seq[Any] = {
    val record = baseRecord.asInstanceOf[ServiceRecord with RatingRecord]
    baseStrategy.makeTrainRow(baseRecord) ++
      record.service.request.payload.asInstanceOf[ProductPayload].quality.values.toList
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any]  = {
    baseStrategy.makeTestRow(init, request) ++
      request.payload.asInstanceOf[ProductPayload].quality.values.toList
  }
}

class BasicContext extends ContextML {
  override val baseStrategy = new BasicML
}

class FireLikeContext extends ContextML {
  override val baseStrategy = new FireLike
}