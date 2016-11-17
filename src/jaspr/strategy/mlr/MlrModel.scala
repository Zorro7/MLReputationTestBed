package jaspr.strategy.mlr

import weka.classifiers.Classifier
import weka.core.Instances

import scala.collection.mutable

/**
  * Created by phil on 05/10/16.
  */
class MlrModel(val model: Classifier,
               val train: Instances,
               val attVals: Iterable[mutable.Map[Any, Double]]
               )
