package jaspr.sellerssim.strategy

import jaspr.core.Network
import jaspr.core.agent.Event
import jaspr.core.provenance.{ServiceRecord, Record}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{StrategyInit, Exploration}
import jaspr.sellerssim.service.BuyerRecord
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
      new TrustAssessment(request, 0d)
    } else {
      val directModel = init.directInit.directModel
      val directTrain = init.directInit.directTrain
      val directAttVals = init.directInit.directAttVals

      val predictions = (for ((fe,p) <- init.directInit.freakEventLikelihood) yield {
        val rows: Iterable[List[Any]] = makeDirectTestRows(init, request, fe)
        val queries = convertRowsToInstances(rows, directAttVals, directTrain)
        val preds = queries.map(q => directModel.classifyInstance(q))
        val vals =
          if (discreteClass) preds.map(x => directTrain.classAttribute().value(x.toInt).toDouble)
          else preds
        vals.map(_ * p)
      }).flatten
      new TrustAssessment(request, predictions.sum/predictions.size)
    }
  }

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val directRecords = context.client.getProvenance[BuyerRecord](context.client)

    val freakEventLikelihood = directRecords.groupBy(_.event.name).mapValues(_.size / directRecords.size.toDouble)

    if (directRecords.isEmpty) null
    else {
      val rows = makeDirectRows(directRecords)
      val directAttVals: Iterable[mutable.Map[Any,Double]] = List.fill(rows.head.size)(mutable.Map[Any,Double]())
      val doubleRows = convertRowsToDouble(rows, directAttVals)
      val atts = makeAtts(rows.head, directAttVals)
      val directTrain = makeInstances(atts, doubleRows)
      val directModel = AbstractClassifier.makeCopy(baseDirect)
      directModel.buildClassifier(directTrain)

      new MlrsDirectInit(context, directModel, directTrain, directAttVals, freakEventLikelihood)
    }
  }


  def makeDirectRows(directRatings: Seq[BuyerRecord]): Iterable[List[Any]] = {
    directRatings.map(x => {
      (if (discreteClass) discretizeInt(x.rating) else x.rating) :: // target rating
//        x.provider.id.toString :: // provider identifier
        x.payload.name :: // service identifier (client context)
        x.event.name :: // mitigation (provider context)
//        x.provider.asInstanceOf[OrganisationAgent].organisation.id.toString ::
        x.provider.advertProperties.values.map(_.value).toList // provider features
    })
  }

  def makeDirectTestRows(init: StrategyInit, request: ServiceRequest, event: String): Iterable[List[Any]] = {
    List(0 ::
//      provider.id.toString ::
      request.payload.name ::
      event ::
//        provider.asInstanceOf[OrganisationAgent].organisation.id.toString ::
      request.provider.advertProperties.values.map(_.value).toList
    )
  }
}
