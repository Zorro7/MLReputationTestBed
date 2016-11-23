package jaspr.sellerssim.strategy.general

import jaspr.core.agent.Provider
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.core.simulation.Network
import jaspr.core.strategy.StrategyInit
import weka.classifiers.Classifier

/**
  * Created by phil on 30/06/16.
  */
trait StereotypeML extends SingleModelStrategy {

  val baseStrategy: SingleModelStrategy
  val payloadAdverts: Boolean

  override val name = this.getClass.getSimpleName + "-" + baseLearner.getClass.getSimpleName+"-"+payloadAdverts

  override def getRecords(network: Network, context: ClientContext): Seq[Record] = {
    baseStrategy.getRecords(network, context)
  }

  override def makeTrainRow(baseRecord: Record): Seq[Any] = {
    val record = baseRecord.asInstanceOf[ServiceRecord with RatingRecord]
    baseStrategy.makeTrainRow(baseRecord) ++
      adverts(record.service.request)
  }

  override def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any] = {
    baseStrategy.makeTestRow(init, request) ++
      adverts(request)
  }

  def adverts(request: ServiceRequest): List[Any] = {
    if (payloadAdverts) request.provider.payloadAdverts(request.payload).values.map(_.value).toList
    else request.provider.generalAdverts.values.map(_.value).toList
  }
}

class BasicStereotype(override val baseLearner: Classifier,
                      override val numBins: Int,
                      override val payloadAdverts: Boolean
                     ) extends StereotypeML {
  override val baseStrategy = new BasicML(baseLearner, numBins, lower, upper)
}

class FireLikeStereotype(override val baseLearner: Classifier,
                         override val numBins: Int,
                         override val payloadAdverts: Boolean
                        ) extends StereotypeML {
  override val baseStrategy = new FireLike(baseLearner, numBins, lower, upper)
}

//class TravosLikeStereotype extends StereotypeML {
//  override val baseStrategy = new TravosLike
//}