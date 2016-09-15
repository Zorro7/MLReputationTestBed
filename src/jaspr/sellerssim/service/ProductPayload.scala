package jaspr.sellerssim.service

import jaspr.core.agent.{Properties, Property}
import jaspr.core.service.Payload

import scala.collection.immutable.{SortedMap, TreeMap}

/**
  * Created by phil on 23/03/16.
  */
class ProductPayload(override val name: String,
                     override val properties: SortedMap[String,Property] = TreeMap(),
                     val quality: SortedMap[String, Double] = TreeMap()
                    ) extends Payload with Properties {

  override def toString: String = name + " " + properties.values.map(_.value)+"-"+quality.values

  def copy(name: String = this.name,
           properties: SortedMap[String,Property] = this.properties,
           quality: SortedMap[String, Double] = this.quality) = {
    new ProductPayload(name, properties, quality)
  }
}
