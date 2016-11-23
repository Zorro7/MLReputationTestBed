package jaspr.sellerssim.strategy.general

import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, Payload, ServiceRequest}
import jaspr.core.simulation.Network
import jaspr.core.strategy.StrategyInit
import jaspr.sellerssim.service.ProductPayload
import weka.classifiers.Classifier

/**
  * Created by phil on 29/06/16.
  */
trait ContextML extends SingleModelStrategy {

  val baseStrategy: SingleModelStrategy
  val usePayloadProperties: Boolean

  override val name = this.getClass.getSimpleName + "-" + baseLearner.getClass.getSimpleName +"-"+ usePayloadProperties

  override def getRecords(network: Network, context: ClientContext): Seq[Record] = {
    baseStrategy.getRecords(network, context)
  }

  override def makeTrainRow(baseRecord: Record): Seq[Any] = {
    val record = baseRecord.asInstanceOf[ServiceRecord with RatingRecord]
    val x = baseStrategy.makeTrainRow(baseRecord) ++ context(record.service.request.payload).reverse
//      (record.service.payload.name :: Nil)
//      (record.service.payload.name :: record.service.request.payload.asInstanceOf[ProductPayload].properties.values.map(_.value).toList)
//        println("train: "+x)
    x
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    val x = baseStrategy.makeTestRow(init, request) ++ context(request.payload).reverse
//      (request.payload.name :: Nil)
//      (request.payload.name :: request.payload.asInstanceOf[ProductPayload].properties.values.map(_.value).toList)
//        println("test: "+x)
    x
  }

  def context(payload: Payload): List[Any] = {
    if (usePayloadProperties) {
      payload.name :: payload.asInstanceOf[ProductPayload].properties.values.map(_.value).toList
    } else {
      payload.name :: Nil
    }
  }

}

class BasicContext(override val baseLearner: Classifier,
                   override val numBins: Int,
                   override val lower: Double,
                   override val upper: Double,
                   override val usePayloadProperties: Boolean) extends ContextML {
  override val baseStrategy = new BasicML(baseLearner, numBins, lower, upper)
}

class FireLikeContext(override val baseLearner: Classifier,
                      override val numBins: Int,
                      override val lower: Double,
                      override val upper: Double,
                      override val usePayloadProperties: Boolean) extends ContextML {
  override val baseStrategy = new FireLike(baseLearner, numBins, lower, upper)
}

//class TravosLikeContext extends ContextML {
//  override val baseStrategy = new TravosLike
//}