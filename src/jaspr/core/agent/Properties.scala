package jaspr.core.agent

/**
 * Created by phil on 15/03/16.
 */
trait Properties {

  def properties: Map[Property, Double]
  def advertProperties: Map[Property, AnyVal]
}


case class Property(name: String)