package jaspr.marketsim.strategy

import jaspr.core.provenance.{RatingRecord, ServiceRecord, TrustAssessmentRecord}
import jaspr.core.service.ClientContext
import jaspr.core.simulation.Network

/**
  * Created by phil on 18/01/2017.
  */
class FireContext(override val witnessWeight: Double = 2d) extends Fire {

  override def getDirectRecords(network: Network, context: ClientContext): Seq[ServiceRecord with RatingRecord with TrustAssessmentRecord] = {
    super.getDirectRecords(network, context).filter(_.service.payload.name == context.payload.name)
  }

  override def getWitnessRecords(network: Network, context: ClientContext): Seq[ServiceRecord with RatingRecord with TrustAssessmentRecord] = {
    super.getWitnessRecords(network, context).filter(_.service.payload.name == context.payload.name)
  }

}
