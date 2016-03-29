package jaspr.sellerssim.service

import jaspr.core.service.Payload

/**
 * Created by phil on 23/03/16.
 */
class ProductPayload(override val name: String, val quality: Map[String,Double] = Map()) extends Payload {

  override def toString(): String = name+" "+quality

  def copy(name: String = name, quality: Map[String,Double] = this.quality) = {
    new ProductPayload(name, quality)
  }
}
