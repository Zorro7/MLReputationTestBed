package jaspr.core.agent

import jaspr.core.Simulation
import jaspr.utilities.NamedEntity

/**
 * Created by phil on 15/03/16.
 */
trait Properties {

  val simulation: Simulation

  def properties: Map[Property, Double]
  def advertProperties: Map[Property, AnyVal]
}


case class Property(name: String)