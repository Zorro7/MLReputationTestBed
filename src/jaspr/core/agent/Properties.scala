package jaspr.core.agent

import jaspr.utilities.NamedEntity
import scala.language.implicitConversions

/**
 * Created by phil on 15/03/16.
 */

trait Properties {

  def properties: Map[String,Property]
}

trait AdvertProperties extends Properties {

  def advertProperties: Map[String,Property]
}

case class Property(override val name: String, value: AnyVal) extends NamedEntity {

  def floatValue: Double = value.asInstanceOf[Float]
  def doubleValue: Double = value.asInstanceOf[Double]
  def byteValue: Byte = value.asInstanceOf[Byte]
  def shortValue: Short = value.asInstanceOf[Short]
  def intValue: Int = value.asInstanceOf[Int]
  def longValue: Long = value.asInstanceOf[Long]
  def charValue: Char = value.asInstanceOf[Char]
  def booleanValue: Boolean = value.asInstanceOf[Boolean]
}
