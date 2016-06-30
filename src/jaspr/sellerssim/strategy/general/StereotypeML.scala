package jaspr.sellerssim.strategy.general

import jaspr.core.Network
import jaspr.core.agent.Provider
import jaspr.core.provenance.{RatingRecord, ServiceRecord, Record}
import jaspr.core.service.{ServiceRequest, ClientContext}
import jaspr.core.strategy.StrategyInit

/**
 * Created by phil on 30/06/16.
 */
trait StereotypeML extends SingleModelStrategy {

  val baseStrategy: SingleModelStrategy

  override def getRecords(network: Network, context: ClientContext): Seq[Record] = {
    baseStrategy.getRecords(network, context)
  }

  override def makeTrainRow(baseRecord: Record): Seq[Any] = {
    val record = baseRecord.asInstanceOf[ServiceRecord with RatingRecord]
    baseStrategy.makeTrainRow(baseRecord) ++
      adverts(record.service.request.provider)
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    baseStrategy.makeTestRow(init, request) ++
      adverts(request.provider)
  }

  def adverts(provider: Provider): List[Any] = {
    provider.advertProperties.values.map(_.value).toList
  }
}

class BasicStereotype extends StereotypeML {
  override val baseStrategy = new BasicML
}

class FireLikeStereotype extends StereotypeML {
  override val baseStrategy = new FireLike
}

//class TravosLikeStereotype extends StereotypeML {
//  override val baseStrategy = new TravosLike
//}