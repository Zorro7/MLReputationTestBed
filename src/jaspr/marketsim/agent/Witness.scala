package jaspr.marketsim.agent

import jaspr.core.agent.Provider
import jaspr.core.provenance.{Provenance, Record}
import jaspr.marketsim.MarketSimulation
import jaspr.utilities.Chooser

/**
  * Created by phil on 03/06/16.
  */
trait Witness extends Provenance {

  val simulation: MarketSimulation

  def witnessModel: WitnessModel = simulation.config.witnessModel(this, simulation.network)

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = {
    if (agent == this) {
      provenance.map(_.asInstanceOf[T])
    } else {
      provenance.withFilter(
        x => !witnessModel.omitRecord(x.asInstanceOf[MarketRecord], agent)
      ).map(
        x => witnessModel.changeRecord(x.asInstanceOf[MarketRecord], agent).asInstanceOf[T]
      )
    }
  }
}

trait WitnessModel {
  def changeRecord(record: MarketRecord, agent: Provenance): MarketRecord

  def omitRecord(record: MarketRecord, agent: Provenance): Boolean
}

class HonestWitnessModel extends WitnessModel {
  def changeRecord(record: MarketRecord, agent: Provenance) = {
    record
  }

  def omitRecord(record: MarketRecord, agent: Provenance) = false
}

class NegationWitnessModel extends WitnessModel {
  def changeRecord(record: MarketRecord, agent: Provenance) = {
    record.copy(
      rating = -record.rating
    )
  }

  def omitRecord(record: MarketRecord, agent: Provenance) = false
}

class PessimisticWitnessModel extends WitnessModel {
  def changeRecord(record: MarketRecord, agent: Provenance) = {
    record.copy(
      rating = (record.rating - 1d) / 2d
    )
  }

  def omitRecord(record: MarketRecord, agent: Provenance) = false
}

class OptimisticWitnessModel extends WitnessModel {
  def changeRecord(record: MarketRecord, agent: Provenance) = {
    record.copy(
      rating = (record.rating + 1d) / 2d
    )
  }

  def omitRecord(record: MarketRecord, agent: Provenance) = false
}



class RandomWitnessModel extends WitnessModel {
  def changeRecord(record: MarketRecord, agent: Provenance) = {
    record.copy(
      rating = Chooser.randomDouble(-1d, 1d)
    )
  }

  def omitRecord(record: MarketRecord, agent: Provenance) = false
}

class PromotionWitnessModel(val agentsToPromote: Seq[Provider]) extends WitnessModel {
  def changeRecord(record: MarketRecord, agent: Provenance) = {
    if (agentsToPromote.contains(record.provider)) {
      record.copy(
        rating = (record.rating + 1d) / 2d
      )
    }
    else record
  }

  def omitRecord(record: MarketRecord, agent: Provenance) = false
}

class SlanderWitnessModel(val agentsToPromote: Seq[Provider]) extends WitnessModel {
  def changeRecord(record: MarketRecord, agent: Provenance) = {
    if (agentsToPromote.contains(record.provider)) {
      record.copy(
        rating = (record.rating - 1d) / 2d
      )
    }
    else record
  }

  def omitRecord(record: MarketRecord, agent: Provenance) = false
}