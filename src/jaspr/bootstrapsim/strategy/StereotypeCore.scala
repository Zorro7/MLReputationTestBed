package jaspr.bootstrapsim.strategy

import jaspr.bootstrapsim.agent.{BootRecord, Trustee, Truster}
import jaspr.core.agent.{Client, FixedProperty, Property, Provider}
import jaspr.core.provenance.Record
import jaspr.core.service.ServiceRequest
import jaspr.core.strategy.StrategyInit
import jaspr.strategy.mlr.{MlrCore, MlrModel}
import jaspr.utilities.BetaDistribution
import weka.classifiers.{AbstractClassifier, Classifier}

import scala.collection.immutable.SortedMap
import scala.collection.mutable


/**
  * Created by phil on 06/10/16.
  */
trait StereotypeCore extends MlrCore {

  val ratingStereotype: Boolean

  def makeStereotypeModels(records: Seq[BootRecord],
                           labels: Map[Provider,Double],
                           baseLearner: Classifier,
                           makeTrainRow: (BootRecord,Map[Provider,Double]) => Seq[Any]
                          ): Map[Client, MlrModel] = {
    records.groupBy(
      _.service.request.client
    ).mapValues(
      rs => {
        makeStereotypeModel(rs, labels, baseLearner, makeTrainRow)
      }
    )
  }

  def makeStereotypeModel(records: Seq[BootRecord],
                          labels: Map[Provider,Double],
                          baseLearner: Classifier,
                          makeTrainRow: (BootRecord,Map[Provider,Double]) => Seq[Any]
                         ): MlrModel = {
    val stereotypeObs: Seq[BootRecord] =
      if (ratingStereotype) records
      else distinctBy[BootRecord,Trustee](records, _.trustee)  // Get the distinct records cause here we assume observations are static for each truster/trustee pair.
    makeMlrsModel[BootRecord](stereotypeObs, baseLearner, makeTrainRow(_: BootRecord, labels))
  }

  override def makeMlrsModel[T <: Record](records: Seq[T], baseModel: Classifier,
                                 makeTrainRow: T => Seq[Any],
                                 makeWeight: T => Double = null): MlrModel = {
    val rows = records.map(makeTrainRow)
    val weights = if (makeWeight == null) Nil else records.map(makeWeight)
    val directAttVals: Iterable[mutable.Map[Any, Double]] = List.fill(rows.head.size)(mutable.Map[Any, Double]("true" -> 1.0, "false" -> 0.0))
    val doubleRows = convertRowsToDouble(rows, directAttVals)
    val atts = makeAtts(rows.head, directAttVals)
    val train = makeInstances(atts, doubleRows, weights)
    val directModel = AbstractClassifier.makeCopy(baseModel)
    directModel.buildClassifier(train)
    new MlrModel(directModel, train, directAttVals)
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

  def makeTrainRow(record: BootRecord, labels: Map[Provider,Double] = Map()): Seq[Any]

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
