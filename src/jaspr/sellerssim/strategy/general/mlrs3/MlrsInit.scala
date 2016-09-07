package jaspr.sellerssim.strategy.general.mlrs3

import jaspr.core.service.ClientContext
import jaspr.core.strategy.StrategyInit
import jaspr.sellerssim.service.BuyerRecord
import weka.classifiers.Classifier
import weka.core.Instances

import scala.collection.mutable

/**
  * Created by phil on 30/03/16.
  */

class MlrsDirectInit(context: ClientContext,
                     val directModel: Classifier,
                     val directTrain: Instances,
                     val directAttVals: Iterable[mutable.Map[Any, Double]],
                     val freakEventLikelihood: Map[String, Double]
                    ) extends StrategyInit(context)

class MlrsWitnessInit(context: ClientContext,
                      val witnessModel: Classifier,
                      val witnessTrain: Instances,
                      val witnessAttVals: Iterable[mutable.Map[Any, Double]],
                      val witnessRatings: Seq[BuyerRecord],
                      val freakEventLikelihood: Map[String, Double]
                     ) extends StrategyInit(context)

class MlrsInit(context: ClientContext,
               val directInit: MlrsDirectInit,
               val witnessInit: MlrsWitnessInit
              ) extends StrategyInit(context)
