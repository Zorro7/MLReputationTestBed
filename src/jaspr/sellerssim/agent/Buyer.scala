package jaspr.sellerssim.agent

import jaspr.core.agent.{Agent, Client}
import jaspr.core.provenance.{RatingRecord, Provenance, Record}
import jaspr.core.service.{Service, TrustAssessment, ClientContext}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.{ProductPayload, BuyerRecord}

/**
 * Created by phil on 21/03/16.
 */
class Buyer(override val simulation: SellerSimulation) extends Client {

  override def generateContext(): ClientContext = {
    simulation.config.clientContext(simulation.network, this, simulation.round)
  }

  override def receiveService(service: Service): Unit = {
    jaspr.debug("RECEIVE: ", service)
    val assessment: TrustAssessment = trustAssessments.remove(service.request) match {
      case Some(x) =>
        val gain = service.utility()
        jaspr.debug(10, "UTILITY: ", simulation.round, this, utility, gain)
        _utility += gain
        x
      case None => throw new Exception("Request "+service.request+" not found.")
    }
    recordProvenance(new BuyerRecord(service, assessment, service.payload.asInstanceOf[ProductPayload].quality))
  }

  override def makeRequest(assessment: TrustAssessment): Unit = {
    jaspr.debug("REQUEST: ", assessment.request)
    assessment.request.provider.receiveRequest(assessment.request)
  }

  override def generateComposition(context: ClientContext): TrustAssessment = {
    simulation.config.strategy.assessReputation(simulation.network, context)
  }

  private var _utility: Double = 0d
  override def utility: Double = _utility

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = {
    def changeRatings(ratings: Map[String,Double]) = {
      ratings.mapValues( - _ )
    }
    def omitRecord(record: BuyerRecord, agent: Provenance): Boolean = {
      false
    }
    if (agent == this) {
      provenance.map(_.asInstanceOf[T])
    } else {
      provenance.withFilter(
        x => !omitRecord(x.asInstanceOf[BuyerRecord], agent)
      ).map(x =>
        x.asInstanceOf[BuyerRecord].copy(ratings = changeRatings(x.asInstanceOf[BuyerRecord].ratings)).asInstanceOf[T]
      )
//      provenanceStore.map(x => x.copy(x.trustAssessment, x.interaction.copyInteraction(
//        ratings = x.interaction.ratings.mapValues(x =>
//          if (liar && noisy) (ifHappens(Liariness)(-x)(x) + randomDouble(-Noisiness,Noisiness)) * excentricity
//          else if (liar) ifHappens(Liariness)(-x)(x) * excentricity
//          else if (noisy) (x + randomDouble(-Noisiness,Noisiness)) * excentricity
//          else x * excentricity
//        )
//      ))).toList
    }
  }

  override val memoryLimit: Int = simulation.config.memoryLimit
}
