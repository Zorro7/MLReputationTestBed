package jaspr.sellerssim.agent

import jaspr.core.agent.Client
import jaspr.core.provenance.{Provenance, Record}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.BuyerRecord
import jaspr.utilities.Chooser

/**
 * Created by phil on 03/06/16.
 */
trait Witness extends Provenance {

  val simulation: SellerSimulation
  def witnessModel: WitnessModel = simulation.config.witnessModel(this)

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = {
    if (agent == this) {
      provenance.map(_.asInstanceOf[T])
    } else {
      provenance.withFilter(
        x => !witnessModel.omitRecord(x.asInstanceOf[BuyerRecord], agent)
      ).map(x =>
        x.asInstanceOf[BuyerRecord].copy(
          ratings = witnessModel.changeRatings(agent, x.asInstanceOf[BuyerRecord].ratings)
        ).asInstanceOf[T]
      )
    }
  }
}

trait WitnessModel {
  def changeRatings(agent: Provenance, ratings: Map[String,Double]): Map[String,Double]
  def omitRecord(record: BuyerRecord, agent: Provenance): Boolean
}

class HonestWitnessModel extends WitnessModel {
  def changeRatings(agent: Provenance, ratings: Map[String,Double]) = ratings
  def omitRecord(record: BuyerRecord, agent: Provenance) = false
}

class PessimisticWitnessModel extends WitnessModel {
  def changeRatings(agent: Provenance, ratings: Map[String,Double]) = {
    ratings.mapValues(x => (x-1)/2)
  }
  def omitRecord(record: BuyerRecord, agent: Provenance) = false
}

class OptimisticWitnessModel extends WitnessModel {
  def changeRatings(agent: Provenance, ratings: Map[String,Double]) = {
    ratings.mapValues(x => (x+1)/2)
  }
  def omitRecord(record: BuyerRecord, agent: Provenance) = false
}

class NegationWitnessModel extends WitnessModel {
  def changeRatings(agent: Provenance, ratings: Map[String,Double]) = {
    ratings.mapValues(- _)
  }
  def omitRecord(record: BuyerRecord, agent: Provenance) = false
}

class RandomWitnessModel extends WitnessModel {
  def changeRatings(agent: Provenance, ratings: Map[String,Double]) = {
    ratings.mapValues(x => Chooser.randomDouble(-1,1))
  }
  def omitRecord(record: BuyerRecord, agent: Provenance) = false
}

class PromotionWitnessModel(val agentsToPromote: Seq[Seller]) extends WitnessModel {
  def changeRatings(agent: Provenance, ratings: Map[String,Double]) = {
    if (agentsToPromote.contains(agent)) ratings.mapValues(x => (x+1)/2)
    else ratings
  }
  def omitRecord(record: BuyerRecord, agent: Provenance) = false
}

class SlanderWitnessModel(val agentsToSlander: Seq[Seller]) extends WitnessModel {
  def changeRatings(agent: Provenance, ratings: Map[String,Double]) = {
    if (agentsToSlander.contains(agent)) ratings.mapValues(x => (x-1)/2)
    else ratings
  }
  def omitRecord(record: BuyerRecord, agent: Provenance) = false
}