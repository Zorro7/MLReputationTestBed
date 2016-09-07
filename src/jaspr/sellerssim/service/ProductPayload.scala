package jaspr.sellerssim.service

import jaspr.core.service.Payload

import scala.collection.immutable.{SortedMap, TreeMap}

/**
  * Created by phil on 23/03/16.
  */
class ProductPayload(override val name: String, val quality: SortedMap[String, Double] = TreeMap()) extends Payload {

  override def toString: String = name + " " + quality

  def copy(name: String = this.name, quality: SortedMap[String, Double] = this.quality) = {
    new ProductPayload(name, quality)
  }
}
