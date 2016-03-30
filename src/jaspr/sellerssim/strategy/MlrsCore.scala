package jaspr.sellerssim.strategy

import java.util.ArrayList

import jaspr.core.provenance.Record
import jaspr.core.provenance.Record
import jaspr.core.service.{ClientContext}
import jaspr.core.strategy.StrategyInit
import jaspr.sellerssim.service.BuyerRecord
import jaspr.utilities.Discretization
import weka.classifiers.evaluation.{NumericPrediction, Prediction, NominalPrediction}
import weka.classifiers.{AbstractClassifier, Classifier}
import weka.core.{Attribute, DenseInstance, Instance, Instances}

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Created by phil on 03/11/15.
 */
trait MlrsCore extends Discretization {

  override val upper = 1d
  override val lower = -1d

  val classIndex: Int = 0
  lazy val discreteClass: Boolean = if (numBins <= 1) false else true

  class MlrsModel(val model: Classifier,
                  val train: Instances,
                  val attVals: Iterable[mutable.Map[Any,Double]]
                   )

  def makeMlrsModel[T <: Record](records: Seq[T], baseModel: Classifier,
                    makeTrainRow: T => Seq[Any],
                    makeWeight: T => Double = (_: T)=>1d): MlrsModel = {
    val rows = records.map(makeTrainRow)
    val weights = records.map(makeWeight)
    val directAttVals: Iterable[mutable.Map[Any,Double]] = List.fill(rows.head.size)(mutable.Map[Any,Double]())
    val doubleRows = convertRowsToDouble(rows, directAttVals)
    val atts = makeAtts(rows.head, directAttVals)
    val directTrain = makeInstances(atts, doubleRows, weights)
    val directModel = AbstractClassifier.makeCopy(baseModel)
    directModel.buildClassifier(directTrain)
    new MlrsModel(directModel, directTrain, directAttVals)
  }

  def evaluateMlrsModel[T <: Record](records: Seq[T], mlrsModel: MlrsModel,
                                     makeTestRow: T => Seq[Any],
                                     makeWeight: T => Double = (_: T)=>0d): Iterable[Prediction] = {
    val rows = records.map(makeTestRow)
    val weights = records.map(makeWeight)
    val queries = convertRowsToInstances(rows, mlrsModel.attVals, mlrsModel.train)
    if (discreteClass) queries.map(q => new NominalPrediction(q.classValue(), mlrsModel.model.distributionForInstance(q)))
    else queries.map(q => new NumericPrediction(q.classValue(), mlrsModel.model.classifyInstance(q)))
  }

  def lookup[T](map: mutable.Map[T,Double], item: T): Double = {
    if (map.contains(item)) map(item)
    else {
      map(item) = map.size
      map(item)
    }
  }

  def convertRowsToDouble(rows: Iterable[Seq[Any]], attVals: Iterable[mutable.Map[Any,Double]]): Iterable[Seq[Double]] = {
    for (row <- rows) yield {
      convertRowToDouble(row, attVals)
    }
  }

  def convertRowToDouble(row: Seq[Any], attVals: Iterable[mutable.Map[Any,Double]]): Seq[Double] = {
    for (((item, vals), i) <- row.zip(attVals).zipWithIndex) yield {
      item match {
        case x: Int => if (i == classIndex) x else lookup(vals, x)
        case x: Double => x
        case x: String => if (i == classIndex) vals(x) else lookup(vals, x)
        case whatever => throw new Exception("Unknown type to build attrbute from.")
      }
    }
  }

  def convertRowsToInstances(rows: Iterable[Seq[Any]], attVals: Iterable[mutable.Map[Any,Double]], dataset: Instances): Iterable[Instance] = {
    rows.map(convertRowToInstance(_, attVals, dataset))
  }

  def convertRowToInstance(row: Seq[Any], attVals: Iterable[mutable.Map[Any,Double]], dataset: Instances): Instance = {
    val inst = new DenseInstance(dataset.numAttributes())
    inst.setDataset(dataset)
    for (((item, vals), i) <- row.zip(attVals).zipWithIndex) {
      item match {
        case x: Int => {
          if (i == classIndex) inst.setValue(i, x.toDouble)
          else inst.setValue(i, vals.getOrElse(x, weka.core.Utils.missingValue))
        }
        case x: Double => inst.setValue(i, x)
        case x: String => inst.setValue(i, vals.getOrElse(x, weka.core.Utils.missingValue))
        case whatever => throw new Exception("Unknown type to build attrbute from.")
      }
    }
    inst
  }

  def makeAtts(row: Seq[Any], attVals: Iterable[mutable.Map[Any,Double]]): Iterable[Attribute] = {
    for (((item,vals),i) <- row.zip(attVals).zipWithIndex) yield {
      if (i==classIndex) if (discreteClass) new Attribute("target", discVals) else new Attribute("target")
      else {
        item match {
          case x: Double => new Attribute(i.toString)
          case x: Int => new Attribute(i.toString, vals.toList.sortBy(_._2).map(_._1.toString))
          case x: String => new Attribute(i.toString, vals.toList.sortBy(_._2).map(_._1.toString))
          case whatever => throw new Exception("Unknown type to build attrbute from.")
        }
      }
    }
  }

  def makeInstances(atts: Iterable[Attribute], doubleRows: Iterable[Seq[Double]], weights: Iterable[Double] = Nil): Instances = {
    val directTrain: Instances = new Instances("data", new ArrayList(atts), doubleRows.size)
    directTrain.setClassIndex(classIndex)
    if (weights.isEmpty) doubleRows.foreach(r => directTrain.add(new DenseInstance(1d, r.toArray)))
    else (doubleRows zip weights).foreach(r => directTrain.add(new DenseInstance(r._2, r._1.toArray)))
    directTrain
  }

}
