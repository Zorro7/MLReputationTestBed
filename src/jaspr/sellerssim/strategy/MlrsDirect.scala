package jaspr.sellerssim.strategy

import jaspr.core.Network
import jaspr.core.agent.{Provider, Event}
import jaspr.core.provenance.{ServiceRecord, Record}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{StrategyInit, Exploration}
import jaspr.sellerssim.service.{ProductPayload, BuyerRecord}
import jaspr.strategy.{Rating, CompositionStrategy}
import weka.classifiers.bayes.NaiveBayes
import weka.classifiers.functions.LinearRegression
import weka.classifiers.trees.J48
import weka.classifiers.{AbstractClassifier, Classifier}

import scala.collection.mutable

/**
 * Created by phil on 04/11/15.
 */
trait MlrsDirect extends CompositionStrategy with Exploration with MlrsCore {


  override val discreteClass: Boolean
  def baseDirect: Classifier


  override def compute(baseInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = baseInit.asInstanceOf[MlrsInit]

    if (init.directInit == null) {
      new TrustAssessment(baseInit.context, request, 0d)
    } else {
      val directModel = init.directInit.directModel
      val directTrain = init.directInit.directTrain
      val directAttVals = init.directInit.directAttVals

      val predictions =
        for ((fe,p) <- init.directInit.freakEventLikelihood) yield {
          val row = makeDirectTestRow(init, request, fe)
          val query = convertRowToInstance(row, directAttVals, directTrain)
          val pred = directModel.classifyInstance(query)
          val result =
            if (discreteClass) {
              val dist = directModel.distributionForInstance(query)
              dist.zipWithIndex.map(x => x._1 * directTrain.classAttribute().value(x._2).toDouble).sum
//              directTrain.classAttribute().value(pred.toInt).toDouble
            }
            else pred
          result * p
        }
      new TrustAssessment(baseInit.context, request, predictions.sum/predictions.size)
    }
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords = context.client.getProvenance[BuyerRecord](context.client)

    if (directRecords.isEmpty) null
    else {
      val model = makeMlrsModel(directRecords, baseDirect, makeDirectRow)
      val freakEventLikelihood = directRecords.groupBy(_.event.name).mapValues(_.size / directRecords.size.toDouble)
      new MlrsDirectInit(context, model.model, model.train, model.attVals, freakEventLikelihood)
    }
  }


  def makeDirectRow(record: BuyerRecord): Seq[Any] = {
    (if (discreteClass) discretizeInt(record.rating) else record.rating) :: // target rating
      record.service.request.payload.name :: // service identifier (client context)
      record.event.name :: // mitigation (provider context)
      record.service.request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
      adverts(record.service.request.provider)
  }

  def makeDirectTestRow(init: StrategyInit, request: ServiceRequest, eventName: String): Seq[Any] = {
    0 ::
      request.payload.name ::
      eventName ::
      request.payload.asInstanceOf[ProductPayload].quality.values.toList ++
      adverts(request.provider)
  }

  def adverts(provider: Provider): List[Any]
  val useAdvertProperties: Boolean = true
}
