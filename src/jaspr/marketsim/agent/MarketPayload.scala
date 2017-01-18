package jaspr.marketsim.agent

import jaspr.core.agent.Property
import jaspr.core.service.Payload

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 18/01/17.
  */
class MarketPayload(override val name: String, val quality: SortedMap[String, Property] = Nil) extends Payload {

  override def toString: String = name + " " + quality.values.map(_.value)

  def copy(name: String = this.name,
           quality: SortedMap[String, Property] = this.quality) = {
    new MarketPayload(name, quality = quality)
  }
}