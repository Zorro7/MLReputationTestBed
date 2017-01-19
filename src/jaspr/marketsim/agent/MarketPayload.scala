package jaspr.marketsim.agent

import jaspr.core.agent.{AdvertProperties, Property}
import jaspr.core.service.Payload

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 18/01/17.
  */
class MarketPayload(override val name: String,
                    override val properties: SortedMap[String, Property] = Nil,
                    override val adverts: SortedMap[String, Property] = Nil) extends Payload with AdvertProperties {

  override def toString: String = name + " " + properties.values.map(_.value)+"-"+adverts.values.map(_.value)

  def copy(name: String = this.name,
           properties: SortedMap[String, Property] = this.properties,
           adverts: SortedMap[String, Property] = this.adverts)= {
    new MarketPayload(name, properties, adverts)
  }
}