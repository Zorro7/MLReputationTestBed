package jaspr.sellerssim.strategy.general

import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.simulation.Network
import jaspr.core.strategy.StrategyInit
import weka.classifiers.Classifier

/**
  * Created by phil on 29/06/16.
  */
class FireLike(override val baseLearner: Classifier,
               override val numBins: Int,
               override val lower: Double,
               override val upper: Double) extends SingleModelStrategy {
  override def getRecords(network: Network, context: ClientContext): Seq[Record] = {
    context.client.getProvenance(context.client) ++ network.gatherProvenance(context.client)
  }

  override def makeTrainRow(baseRecord: Record): Seq[Any] = {
    val record = baseRecord.asInstanceOf[ServiceRecord with RatingRecord]
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.service.request.provider.name :: // service identifier (client context)
      Nil
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    0d ::
      request.provider.name ::
      Nil
  }
}
