package jaspr.acmelogistics.service

import java.text.DecimalFormat

import jaspr.core.service.Payload

/**
 * Created by phil on 17/03/16.
 */
case class GoodPayload(quality: Double, quantity: Double) extends Payload {

  val df = new DecimalFormat("#.##")

  override def toString(): String = {
    "Good("+df.format(quality)+"x"+df.format(quantity)+")"
  }
}
