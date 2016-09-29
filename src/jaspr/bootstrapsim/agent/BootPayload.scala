package jaspr.bootstrapsim.agent

import jaspr.core.agent.{Properties, Property}
import jaspr.core.service.Payload

import scala.collection.immutable.{TreeMap, SortedMap}

/**
  * Created by phil on 29/09/2016.
  */
class BootPayload(override val name: String, override val properties: SortedMap[String,Property] = Nil) extends Payload with Properties {

  override def toString: String = name + " " + properties.values.map(_.value)+"-"+properties.values

  def copy(name: String = this.name,
           properties: SortedMap[String,Property] = this.properties) = {
    new BootPayload(name, properties)
  }
}
