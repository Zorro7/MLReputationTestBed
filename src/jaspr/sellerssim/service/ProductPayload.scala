package jaspr.sellerssim.service

import jaspr.core.service.Payload

/**
 * Created by phil on 23/03/16.
 */
class ProductPayload(override val name: String) extends Payload {

  var quality: Map[String,Double] = Map()

  override def toString: String = name+"Product"
}
