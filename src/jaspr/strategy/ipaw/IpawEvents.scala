package jaspr.strategy.ipaw

import jaspr.acmelogistics.agent.ACMEEvent
import jaspr.acmelogistics.service.{GoodPayload, SubproviderRecord}
import jaspr.core.Network
import jaspr.core.agent.Provider
import jaspr.core.provenance.{RatingRecord, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.strategy.{Exploration, Strategy, StrategyInit}
import jaspr.utilities.MultiRegression
import weka.classifiers.Classifier

/**
 * Created by phil on 19/03/16.
 */
class IpawEvents(learner: Classifier, disc: Boolean) extends Strategy with Exploration with IpawCore {

  override val explorationProbability: Double = 0.1

  val baseLearner = new MultiRegression()
  baseLearner.setBase(learner)
  baseLearner.setSplitAttIndex(1)
  val discreteClass: Boolean = disc

  override val name = this.getClass.getSimpleName+"_"+baseLearner.getClass.getSimpleName


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

    val eventLikelihoods =
      records.groupBy(x =>
        x.service.serviceContext.events.headOption match {
          case Some(e) => e.getClass.getName
          case None => ""
        }
      ).mapValues(_.size.toDouble / records.size)

    println(eventLikelihoods)

    new IpawInit(context, records, models, eventLikelihoods)
  }

  override def computeAssessment(superInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = superInit.asInstanceOf[IpawInit]

    if (init.models.isEmpty) return new TrustAssessment(request, 0d)

    val requests = request.flatten()
    val events = init.eventLikelihoods.keys.toList

    var currentPreds =
      if (init.models.head.forall(_ != null)) {
        val test = events.map(event =>
          makeBaseRow(requests.head, event) ++
            requests.head.provider.asInstanceOf[Provider].advertProperties.values.map(_.value).toList
        )
        init.models.head.map(predicts(_, test, init.eventLikelihoods))
      } else {
        init.models.head.map(x => Double.NaN)
      }

    var preds: List[Double] = currentPreds.toList

    for ((model,request) <- init.models.drop(1) zip requests.drop(1)) {
      currentPreds =
        if (model.forall(_ != null)) {
          val test = events.map(event =>
            makeBaseRow(request, event) ++
              currentPreds ++
              request.provider.asInstanceOf[Provider].advertProperties.values.map(_.value).toList
          )
          model.map(predicts(_, test, init.eventLikelihoods))
        } else {
          model.map(x => Double.NaN)
        }
      preds = currentPreds.toList ++ preds
    }

//        println(preds.filterNot(_.isNaN).sum, currentPreds.filterNot(_.isNaN).sum,
//                  currentPreds.filterNot(_.isNaN), preds.filterNot(_.isNaN))

    new TrustAssessment(request, currentPreds.filter(!_.isNaN).sum)
  }

  def makeBaseRow(request: ServiceRequest, event: String): List[Any] = {
    makeRow(0d, event,
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
          x.service.serviceContext.events.headOption match {
            case Some(e) => e.getClass.getName
            case None => ""
          },
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
          x.service.serviceContext.events.headOption match {
            case Some(e) => e.getClass.getName
            case None => ""
          },
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
