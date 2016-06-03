package jaspr.sellerssim.agent

import jaspr.core.agent.Client
import jaspr.core.provenance.{Provenance, Record}
import jaspr.sellerssim.SellerSimulation
import jaspr.sellerssim.service.BuyerRecord

/**
 * Created by phil on 03/06/16.
 */
trait Witness extends Client {
  val simulation: SellerSimulation

  def changeRatings: Map[String,Double] => Map[String,Double] = simulation.config.changeRatings(this)


  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = {
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
    }
  }
}
