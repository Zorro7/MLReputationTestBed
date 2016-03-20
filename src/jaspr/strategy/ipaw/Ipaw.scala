package jaspr.strategy.ipaw

import jaspr.acmelogistics.service.{SubproviderRecord, GoodPayload}
import jaspr.core.Network
import jaspr.core.agent.Provider
import jaspr.core.provenance.{RatingRecord, ServiceRecord, Record}
import jaspr.core.service.{ClientContext, TrustAssessment, ServiceRequest}
import jaspr.core.strategy.{StrategyInit, Exploration, Strategy}
import weka.classifiers.{Classifier, AbstractClassifier}

import scala.collection.mutable

/**
 * Created by phil on 19/03/16.
 */
class Ipaw(learner: Classifier, disc: Boolean) extends Strategy with Exploration with IpawCore {

  override val explorationProbability: Double = 0.2

  override val name = this.getClass.getSimpleName+"_"+learner.getClass.getSimpleName

  val baseLearner = learner
  val discreteClass: Boolean = disc


  def meanFunch(x: ServiceRecord): Double = x.service.utility()
  def startFunch(x: ServiceRecord): Double = (x.service.request.start - x.service.start).toDouble
  def endFunch(x: ServiceRecord): Double = (x.service.request.end - x.service.end).toDouble
  def qualityFunch(x: ServiceRecord): Double =
    x.service.request.payload.asInstanceOf[GoodPayload].quality - x.service.payload.asInstanceOf[GoodPayload].quality
  def quantityFunch(x: ServiceRecord): Double =
    x.service.request.payload.asInstanceOf[GoodPayload].quantity - x.service.payload.asInstanceOf[GoodPayload].quantity

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val records: Seq[ServiceRecord] = network.gatherProvenance(context.client)

    val baseModel =
//          buildBaseModel(records, meanFunch) ::
    //      buildBaseModel(records, startFunch) ::
      buildBaseModel(records, endFunch) ::
        buildBaseModel(records, qualityFunch) ::
        buildBaseModel(records, quantityFunch) ::
        Nil
    val topModel =
//          buildTopModel(records, meanFunch) ::
    //      buildTopModel(records, startFunch) ::
      buildTopModel(records, endFunch) ::
        buildTopModel(records, qualityFunch) ::
        buildTopModel(records, quantityFunch) ::
        Nil

//    if (baseModel.head != null) {
//      println(baseModel.head.train)
//      println(baseModel.head.model)
//    }
//    if (topModel.head != null) {
//      println(topModel.head.train)
//      println(topModel.head.model)
//    }

    new IpawInit(context, records, baseModel, topModel)
  }

  override def computeAssessment(superInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = superInit.asInstanceOf[IpawInit]

    var requests = request.flatten()
//    println(requests)

    var currentPreds =
      if (init.baseModel.forall(_ != null)) {
        val mineTest =
          makeBaseRow(requests.head) ++
            requests.head.provider.asInstanceOf[Provider].advertProperties.values.map(_.value).toList
        init.baseModel.map(predict(_, mineTest))
      } else {
        init.baseModel.map(x => Double.NaN)
      }

    var preds: List[Double] = currentPreds.toList

    for (request <- requests.drop(1)) {
//      println(currentPreds)
      currentPreds =
        if (init.topModel.forall(_ != null)) {
          val shipTest =
            makeBaseRow(request) ++
              currentPreds ++
              request.provider.asInstanceOf[Provider].advertProperties.values.map(_.value).toList
//          println(init.topModel.head.train)
//          println(shipTest)
          init.topModel.map(predict(_, shipTest))
        } else {
          init.topModel.map(x => Double.NaN)
        }
      preds = currentPreds.toList ++ preds
    }

        println(preds.filterNot(_.isNaN).sum, currentPreds.filterNot(_.isNaN).sum,
                  currentPreds.filterNot(_.isNaN), preds.filterNot(_.isNaN))

    new TrustAssessment(request, currentPreds.filter(!_.isNaN).sum)
  }







  def makeBaseRow(request: ServiceRequest): List[Any] = {
    makeRow(0d,
      //    mineR.start.toDouble,
      request.duration.toDouble,
      request.payload.asInstanceOf[GoodPayload].quality,
      request.payload.asInstanceOf[GoodPayload].quantity
    )
  }


  def buildBaseModel(records: Iterable[ServiceRecord],
                     labelfunch: ServiceRecord => Double
                      ): IpawModel = {
    val train: Iterable[List[Any]] =
      records.map(x => {
        makeRow(
          labelfunch(x),
          //          x.request.start.toDouble,
          x.service.request.duration.toDouble,
          x.service.request.payload.asInstanceOf[GoodPayload].quality,
          x.service.request.payload.asInstanceOf[GoodPayload].quantity
        ) ++ x.service.request.provider.advertProperties.values.map(_.value).toList
      })
    build(train)
  }

  def buildTopModel(records: Iterable[ServiceRecord],
                    labelfunch: ServiceRecord => Double
                     ): IpawModel = {
    val train: Iterable[List[Any]] =
      records.withFilter(!_.service.serviceContext.provenanceEmpty).map(x => {
        val dependency: SubproviderRecord = x.service.serviceContext.getProvenance(x.service.serviceContext).head
//        println(x.service.payload , dependency.service.payload)
        makeRow(
          labelfunch(x),
          //          x.request.start.toDouble,
          x.service.request.duration.toDouble,
          x.service.request.payload.asInstanceOf[GoodPayload].quality,
          x.service.request.payload.asInstanceOf[GoodPayload].quantity
          //          ,meanFunch(mineDependency)
          //          , startFunch(mineDependency)
          ,endFunch(dependency)
          ,qualityFunch(dependency),
          quantityFunch(dependency)
        ) ++ x.service.request.provider.advertProperties.values.map(_.value).toList
      })
    build(train)
  }


  def build(trainRows: Iterable[List[Any]]): IpawModel = {
    if (trainRows.nonEmpty) {
      val attVals: Iterable[mutable.Map[Any, Double]] = List.fill(trainRows.head.size)(mutable.Map[Any, Double]())
      val doubleRows = convertRowsToDouble(trainRows, attVals)
      val atts = makeAtts(trainRows.head, attVals)
      val train = makeInstances(atts, doubleRows)
      val model = AbstractClassifier.makeCopy(baseLearner)
      model.buildClassifier(train)
      //      println(train)
      //      println(model)
      new IpawModel(model, attVals, train)
    } else {
      null
    }
  }

  def predict(model: IpawModel, testRow: List[Any]): Double = {
    //    val queries = convertRowsToInstances(testRows, model.attVals, model.train)
    //    queries.map(x => model.model.classifyInstance(x))
    val x = convertRowToInstance(testRow, model.attVals, model.train)
    model.model.classifyInstance(x)
  }

}
