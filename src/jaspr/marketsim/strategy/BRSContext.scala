package jaspr.marketsim.strategy

import jaspr.bootstrapsim.strategy.BRS
import jaspr.core.provenance.{RatingRecord, ServiceRecord}
import jaspr.core.service.ClientContext
import jaspr.core.simulation.Network

/**
  * Created by phil on 18/01/2017.
  */
class BRSContext(override val witnessWeight: Double = 2d) extends BRS {

  override def getDirectRecords(network: Network, context: ClientContext): Seq[ServiceRecord with RatingRecord] = {
    context.client.getProvenance[ServiceRecord with RatingRecord](context.client).filter(_.service.payload.name == context.payload.name)
  }

  override def getWitnessRecords(network: Network, context: ClientContext): Seq[ServiceRecord with RatingRecord] = {
    network.gatherProvenance[ServiceRecord with RatingRecord](context.client).filter(_.service.payload.name == context.payload.name)
  }

}
