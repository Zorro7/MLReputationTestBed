package jaspr.sellerssim.dynamicsimulation

import jaspr.core.results.Result
import jaspr.sellerssim.SellerSimulation
import jaspr.utilities.Tickable

/**
  * Created by phil on 08/09/16.
  */
class DynamicSellerSimulation(override val config: DynamicSellerConfiguration) extends SellerSimulation(config) {

  override def act(): Result = {
    network match {
      case x: Tickable => x.tick()
      case _ => // do nothing
    }

    super.act()
  }

}
