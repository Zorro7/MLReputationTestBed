package jaspr.sellerssim.agent

import jaspr.core.agent.Provider
import jaspr.core.provenance.{Provenance, Record}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.BuyerRecord
import jaspr.utilities.Chooser

/**
 * Created by phil on 03/06/16.
 */
trait Witness extends Provenance {

  val simulation: SellerSimulation
  def witnessModel: WitnessModel = simulation.config.witnessModel(this, simulation.network)

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = {
    if (agent == this) {
      provenance.map(_.asInstanceOf[T])
    } else {
      provenance.withFilter(
        x => !witnessModel.omitRecord(x, agent)
      ).map(
        x => witnessModel.changeRecord(x, agent).asInstanceOf[T]
      )
    }
  }
}

trait WitnessModel {
  def changeRecord(record: Record, agent: Provenance): Record
  def omitRecord(record: Record, agent: Provenance): Boolean
}

class HonestWitnessModel extends WitnessModel {
  def changeRecord(record: Record, agent: Provenance) = record
  def omitRecord(record: Record, agent: Provenance) = false
}

class PessimisticWitnessModel extends WitnessModel {
  def changeRecord(record: Record, agent: Provenance) = {
    record.asInstanceOf[BuyerRecord].copy(
      ratings = record.asInstanceOf[BuyerRecord].ratings.mapValues(x => (x-1d)/2d)
    )
  }
  def omitRecord(record: Record, agent: Provenance) = false
}

class OptimisticWitnessModel extends WitnessModel {
  def changeRecord(record: Record, agent: Provenance) = {
    record.asInstanceOf[BuyerRecord].copy(
      ratings = record.asInstanceOf[BuyerRecord].ratings.mapValues(x => (x+1d)/2d)
    )
  }
  def omitRecord(record: Record, agent: Provenance) = false
}

class NegationWitnessModel extends WitnessModel {
  def changeRecord(record: Record, agent: Provenance) = {
    record.asInstanceOf[BuyerRecord].copy(
      ratings = record.asInstanceOf[BuyerRecord].ratings.mapValues(x => -x)
    )
  }
  def omitRecord(record: Record, agent: Provenance) = false
}

class RandomWitnessModel extends WitnessModel {
  def changeRecord(record: Record, agent: Provenance) = {
    record.asInstanceOf[BuyerRecord].copy(
      ratings = record.asInstanceOf[BuyerRecord].ratings.mapValues(x => Chooser.randomDouble(-1d,1d))
    )
  }
  def omitRecord(record: Record, agent: Provenance) = false
}

class PromotionWitnessModel(val agentsToPromote: Seq[Provider]) extends WitnessModel {
  def changeRecord(record: Record, agent: Provenance) = {
    if (agentsToPromote.contains(record.asInstanceOf[BuyerRecord].provider)) {
      record.asInstanceOf[BuyerRecord].copy(
        ratings = record.asInstanceOf[BuyerRecord].ratings.mapValues(x => (x+1d)/2d)
      )
    }
    else record
  }
  def omitRecord(record: Record, agent: Provenance) = false
}

class SlanderWitnessModel(val agentsToPromote: Seq[Provider]) extends WitnessModel {
  def changeRecord(record: Record, agent: Provenance) = {
    if (agentsToPromote.contains(record.asInstanceOf[BuyerRecord].provider)) {
      record.asInstanceOf[BuyerRecord].copy(
        ratings = record.asInstanceOf[BuyerRecord].ratings.mapValues(x => (x-1d)/2d)
      )
    }
    else record
  }
  def omitRecord(record: Record, agent: Provenance) = false
}