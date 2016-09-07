package jaspr.acmelogistics.strategy.ipaw

import jaspr.acmelogistics.service.{GoodPayload, SubproviderRecord}
import jaspr.core.agent.Provider
import jaspr.core.provenance.{RatingRecord, Record, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, Strategy, StrategyInit}
import jaspr.weka.classifiers.meta.MultiRegression
import weka.classifiers.{AbstractClassifier, Classifier}
/**
 * Created by phil on 19/03/16.
 */
class Ipaw(learner: Classifier, disc: Boolean) extends Strategy with Exploration with IpawCore {

  override val explorationProbability: Double = 0.1

  val baseLearner = learner
  val discreteClass: Boolean = disc

  override val name = this.getClass.getSimpleName+"_"+baseLearner.getClass.getSimpleName

  override def initStrategy(network: Network, context: ClientContext): StrategyInit = {
    val records: Seq[ServiceRecord with RatingRecord] =
      context.client.getProvenance(context.client) ++ network.gatherProvenance(context.client)

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

    new IpawInit(context, records, models, Map())
  }

  override def computeAssessment(superInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = superInit.asInstanceOf[IpawInit]

    if (init.models.isEmpty) return new TrustAssessment(superInit.context, request, 0d)

    val requests = request.flatten()

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

//    println(preds.filterNot(_.isNaN).sum, currentPreds.filterNot(_.isNaN).sum,
//      preds, currentPreds)

    new TrustAssessment(superInit.context, request, currentPreds.filter(!_.isNaN).sum)
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




}
