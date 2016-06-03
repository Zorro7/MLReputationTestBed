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

  def changeRatings(agent: Provenance, ratings: Map[String,Double]): Map[String,Double] = {
    ratings
  }

  def omitRecord(record: BuyerRecord, agent: Provenance): Boolean = {
    false
  }

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = {
    if (agent == this) {
      provenance.map(_.asInstanceOf[T])
    } else {
      provenance.withFilter(
        x => !omitRecord(x.asInstanceOf[BuyerRecord], agent)
      ).map(x =>
        x.asInstanceOf[BuyerRecord].copy(
          ratings = changeRatings(agent, x.asInstanceOf[BuyerRecord].ratings)
        ).asInstanceOf[T]
      )
    }
  }
}
