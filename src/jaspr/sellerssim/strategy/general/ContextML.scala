package jaspr.sellerssim.strategy.general

import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.simulation.Network
import jaspr.core.strategy.StrategyInit
import jaspr.sellerssim.service.ProductPayload
import weka.classifiers.Classifier

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
    val x = baseStrategy.makeTrainRow(baseRecord) ++
      (record.service.payload.name :: Nil)
//      (record.service.payload.name :: record.service.request.payload.asInstanceOf[ProductPayload].properties.values.map(_.value).toList)
//        println("train: "+x)
    x
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    val x = baseStrategy.makeTestRow(init, request) ++
      (request.payload.name :: Nil)
//      (request.payload.name :: request.payload.asInstanceOf[ProductPayload].properties.values.map(_.value).toList)
    //    println("test: "+x)
    x
  }
}

class BasicContext(override val baseLearner: Classifier, override val numBins: Int) extends ContextML {
  override val baseStrategy = new BasicML(baseLearner, numBins)
}

class FireLikeContext(override val baseLearner: Classifier, override val numBins: Int) extends ContextML {
  override val baseStrategy = new FireLike(baseLearner, numBins)
}

//class TravosLikeContext extends ContextML {
//  override val baseStrategy = new TravosLike
//}