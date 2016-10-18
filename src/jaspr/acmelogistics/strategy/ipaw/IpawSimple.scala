package jaspr.acmelogistics.strategy.ipaw

import jaspr.acmelogistics.service.GoodPayload
import jaspr.core.agent.Provider
import jaspr.core.provenance.{RatingRecord, ServiceRecord}
import jaspr.core.service.{ClientContext, ServiceRequest, TrustAssessment}
import jaspr.core.simulation.Network
import jaspr.core.strategy.{Exploration, Strategy, StrategyInit}
import weka.classifiers.Classifier

/**
  * Created by phil on 04/04/16.
  */
class IpawSimple(learner: Classifier, disc: Boolean) extends Strategy with Exploration with IpawCore {

  override val explorationProbability: Double = 0.1

  val baseLearner = learner
  val discreteClass: Boolean = disc

  override val name = this.getClass.getSimpleName + "_" + baseLearner.getClass.getSimpleName

  override def initStrategy(network: Network, context: ClientContext, requests: Seq[ServiceRequest]): StrategyInit = {
    val records: Seq[ServiceRecord with RatingRecord] =
      context.client.getProvenance(context.client) ++ network.gatherProvenance(context.client)

    val models: Seq[Seq[IpawModel]] =
      if (records.nonEmpty) {
        val requests: Seq[ServiceRequest] = records.head.service.request.flatten()

        requests.map(request =>
          buildBaseModel(records.filter(_.service.request.provider.getClass == request.provider.getClass), endFunch) ::
            buildBaseModel(records.filter(_.service.request.provider.getClass == request.provider.getClass), qualityFunch) ::
            buildBaseModel(records.filter(_.service.request.provider.getClass == request.provider.getClass), quantityFunch) ::
            Nil
        )
      } else Nil

    new IpawInit(context, records, models, Map())
  }


  override def computeAssessment(superInit: StrategyInit, request: ServiceRequest): TrustAssessment = {
    val init = superInit.asInstanceOf[IpawInit]

    if (init.models.isEmpty) return new TrustAssessment(superInit.context, request, 0d)

    val requests = request.flatten()

    var preds: List[Double] = Nil

    for ((model, request) <- init.models zip requests) {
      val currentPreds =
        if (model.forall(_ != null)) {
          val test =
            makeBaseRow(request) ++
              request.provider.asInstanceOf[Provider].generalAdverts.values.map(_.value).toList
          model.map(predict(_, test))
        } else {
          model.map(x => Double.NaN)
        }
      preds = currentPreds.toList ++ preds
    }
    new TrustAssessment(superInit.context, request, preds.filter(!_.isNaN).sum)
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
        ) ++ x.service.request.provider.generalAdverts.values.map(_.value).toList
      })
    build(train)
  }
}
