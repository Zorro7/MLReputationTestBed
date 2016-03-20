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

  override val explorationProbability: Double = 0.1

  override val name = this.getClass.getSimpleName+"_"+learner.getClass.getSimpleName

  val baseLearner = learner
  val discreteClass: Boolean = disc


  def meanFunch(x: RatingRecord): Double = x.rating
  def startFunch(x: ServiceRecord): Double = (x.service.start - x.service.request.start).toDouble
  def endFunch(x: ServiceRecord): Double =
    (x.service.end - x.service.request.end).toDouble / (x.service.request.end - x.service.request.start).toDouble
  def qualityFunch(x: ServiceRecord): Double =
    x.service.payload.asInstanceOf[GoodPayload].quality - x.service.request.payload.asInstanceOf[GoodPayload].quality
  def quantityFunch(x: ServiceRecord): Double =
    x.service.payload.asInstanceOf[GoodPayload].quantity - x.service.request.payload.asInstanceOf[GoodPayload].quantity

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val records: Seq[ServiceRecord with RatingRecord] = network.gatherProvenance(context.client)

    val models: Seq[Seq[IpawModel]] =
      if (records.nonEmpty) {
        val requests: Seq[ServiceRequest] = records.head.service.request.flatten()

        val baseModels: Seq[IpawModel] =
          buildBaseModel(records.filter(_.service.request.provider.getClass == requests.head.provider.getClass), endFunch) ::
          buildBaseModel(records.filter(_.service.request.provider.getClass == requests.head.provider.getClass), qualityFunch) ::
          buildBaseModel(records.filter(_.service.request.provider.getClass == requests.head.provider.getClass), quantityFunch) ::
          Nil

        val topModels: Seq[Seq[IpawModel]] =
          requests.drop(1).map(request =>
            buildTopModel(records.filter(_.service.request.provider.getClass == request.provider.getClass), endFunch) ::
            buildTopModel(records.filter(_.service.request.provider.getClass == request.provider.getClass), qualityFunch) ::
            buildTopModel(records.filter(_.service.request.provider.getClass == request.provider.getClass), quantityFunch) ::
            Nil
          )

        baseModels :: topModels.toList
    } else Nil

    new IpawInit(context, records, models)
  }

  override def computeAssessment(superInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = superInit.asInstanceOf[IpawInit]

    if (init.models.isEmpty) return new TrustAssessment(request, 0d)

    var requests = request.flatten()

    var currentPreds =
      if (init.models.head.forall(_ != null)) {
        val test =
          makeBaseRow(requests.head) ++
            requests.head.provider.asInstanceOf[Provider].advertProperties.values.map(_.value).toList
        init.models.head.map(predict(_, test))
      } else {
        init.models.head.map(x => Double.NaN)
      }

    var preds: List[Double] = currentPreds.toList

    for ((model,request) <- init.models.drop(1) zip requests.drop(1)) {
      currentPreds =
        if (model.forall(_ != null)) {
          val test =
            makeBaseRow(request) ++
              currentPreds ++
              request.provider.asInstanceOf[Provider].advertProperties.values.map(_.value).toList
          model.map(predict(_, test))
        } else {
          model.map(x => Double.NaN)
        }
      preds = currentPreds.toList ++ preds
    }

//        println(preds.filterNot(_.isNaN).sum, currentPreds.filterNot(_.isNaN).sum,
//                  currentPreds.filterNot(_.isNaN), preds.filterNot(_.isNaN))

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


  def buildBaseModel(records: Iterable[ServiceRecord with RatingRecord],
                     labelfunch: ServiceRecord with RatingRecord => Double
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

  def buildTopModel(records: Iterable[ServiceRecord with RatingRecord],
                    labelfunch: ServiceRecord with RatingRecord => Double
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
//          ,meanFunch(dependency)
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
