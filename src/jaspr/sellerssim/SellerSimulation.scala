package jaspr.sellerssim

import jaspr.core.results.Result
import jaspr.core.{Network, Simulation}
import jaspr.sellerssim.agent.Seller
import jaspr.utilities.Chooser

/**
 * Created by phil on 21/03/16.
 */
object SellerSimulation extends App {
  val results = Simulation(new SellerMultiConfiguration)


//  def sigmoid(x: Double) = x/(1d+Math.exp(Math.abs(x)))
//
//  val results = for (i <- 0 to 10000) yield {
//    val request = Chooser.randomDouble(-1d,1d)
//    val delivered = Chooser.randomDouble(-1d,1d)
////    val utility = 1d-Math.abs(delivered - request)
////    var utility = (request - delivered) / Math.max(request, delivered)
//    val utility =
//      if (request < 0d && delivered < 0d) {
//        0d
//      } else {
//        0d
//      }
////    utility = sigmoid(utility)
//    println(request, delivered, utility)
//    utility
//  }
//  println("MEAN: "+results.sum/results.size.toDouble)
//

}

class SellerSimulation(val config: SellerConfiguration) extends Simulation {

  override val network: Network = new SellerNetwork(this)

  override def act(): Result = {
    for (client <- network.clients) {
      Chooser.ifHappens(config.clientInvolvementLikelihood)(
        client.tick()
      )()
    }

    for (provider <- network.providers) {
      provider.tick()
    }

    new Result(this)
  }

}
