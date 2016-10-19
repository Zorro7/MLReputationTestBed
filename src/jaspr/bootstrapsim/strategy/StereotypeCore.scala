package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.{BootRecord, Trustee, Truster}
import jaspr.core.agent.{Client, FixedProperty, Property, Provider}
import jaspr.core.provenance.Record
import jaspr.core.service.ServiceRequest
import jaspr.core.strategy.StrategyInit
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.BetaDistribution
import weka.classifiers.Classifier

import scala.collection.immutable.SortedMap


/**
  * Created by phil on 06/10/16.
  */
trait StereotypeCore extends MlrCore {

  val contractStereotypes: Boolean

  def makeStereotypeModels(records: Seq[BootRecord],
                           baseLearner: Classifier,
                           makeTrainRow: BootRecord => Seq[Any]
                          ): Map[Client, MlrModel] = {
    records.groupBy(
      _.service.request.client
    ).mapValues(
      rs => {
        val stereotypeObs: Seq[BootRecord] =
          if (contractStereotypes) rs
          else distinctBy[BootRecord,Trustee](rs, _.trustee)  // Get the distinct records cause here we assume observations are static for each truster/trustee pair.
        makeMlrsModel[BootRecord](stereotypeObs, baseLearner, makeTrainRow)
      }
    )
  }

  def distinctBy[T,P](xs: Iterable[T], f: T => P) = {
    xs.foldRight((List[T](), Set[P]())) {
      case (o, cum@(objects, props)) =>
        if (props(f(o))) cum else (o :: objects, props + f(o))
    }._1
  }

  def computeStereotypeWeight(model: MlrModel, betas: Map[Provider,BetaDistribution]): Double = {
    val sqrdiff = betas.map(b => {
      val exp = b._2.expected()
      val row = 0d :: adverts(b._1)
      val query = convertRowToInstance(row, model.attVals, model.train)
      val pred = makePrediction(query, model)
      (exp-pred)*(exp-pred)
    }).sum
    1-Math.sqrt(sqrdiff / model.train.size.toDouble)
  }

  def makeTrainRow(record: BootRecord): Seq[Any]

  def makeTestRow(init: StrategyInit, request: ServiceRequest): Seq[Any]


  // Returns the features of provider from the perspective of client.
  // Used for testing only!!!
  def featureTest(client: Client, provider: Provider) = {
    val truster = client.asInstanceOf[Truster]
    val features: SortedMap[String,Property] = provider.generalAdverts.map(x => {
      if (truster.properties.contains(x._1) && truster.properties(x._1).booleanValue) {
        x._2
      } else if (truster.properties.contains(x._1) && !truster.properties(x._1).booleanValue) {
        FixedProperty(x._1, !x._2.booleanValue)
      } else FixedProperty(x._1, false)
    }).toList
    features.values.map(_.value.toString).toList
  }


  def adverts(provider: Provider): List[Any] = {
    provider.generalAdverts.values.map(_.value.toString).toList
  }

  def adverts(request: ServiceRequest): List[Any] = {
    request.properties.values.map(_.value.toString).toList
  }
}
