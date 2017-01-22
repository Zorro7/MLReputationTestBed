package jaspr.marketsim.agent

import jaspr.core.provenance.{Provenance, Record}
import jaspr.marketsim.MarketSimulation

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

class ObjectiveWitnessModel extends WitnessModel {
  def changeRecord(record: MarketRecord, agent: Provenance) = record

  def omitRecord(record: MarketRecord, agent: Provenance) = false
}

class NegativeWitnessModel extends WitnessModel {
  def changeRecord(record: MarketRecord, agent: Provenance) = {
    record.copy(rating = -record.rating)
  }

  def omitRecord(record: MarketRecord, agent: Provenance) = false
}