package jaspr.core.agent

import jaspr.utilities.NamedEntity

/**
 * Created by phil on 15/03/16.
 */

trait Properties {

  def properties: Map[String,Property]

  def property(key: String): Property = {
    properties.get(key).get
  }
}

trait AdvertProperties extends Properties {

  def advertProperties: Map[String,Property]

  def advert(key: String): Property = {
    advertProperties.get(key).get
  }
}

case class Property(override val name: String, var value: AnyVal) extends NamedEntity {

  // todo consider implementing float byte Value etc.
//  def floatValue: Float = value.asInstanceOf[Float]
//  def byteValue: Byte = value.asInstanceOf[Byte]
//  def shortValue: Short = value.asInstanceOf[Short]
//  def longValue: Long = value.asInstanceOf[Long]
//  def charValue: Char = value.asInstanceOf[Char]
  def booleanValue: Boolean = {
    value match {
      case x: Boolean => x.asInstanceOf[Boolean]
      case x: Double => x.asInstanceOf[Double] > 0d
      case x: Int => x.asInstanceOf[Int] > 0
      case x: Long => x.asInstanceOf[Long] > 0
      case x: Float => x.asInstanceOf[Float] > 0f
      case x: Byte => x.asInstanceOf[Byte] > 0
      case x: Short => x.asInstanceOf[Short] > 0
      case x: Char => x.asInstanceOf[Char] > 0
    }
  }

  def doubleValue: Double = {
    value match {
      case x: Double => x.asInstanceOf[Double]
      case x: Int => x.asInstanceOf[Int]
      case x: Boolean => if (x.asInstanceOf[Boolean]) 1d else 0d
      case x: Long => x.asInstanceOf[Long]
      case x: Float => x.asInstanceOf[Float]
      case x: Byte => x.asInstanceOf[Byte]
      case x: Short => x.asInstanceOf[Short]
      case x: Char => x.asInstanceOf[Char]
    }
  }

  def intValue: Int = {
    value match {
      case x: Int => x.asInstanceOf[Int]
      case x: Boolean => if (x.asInstanceOf[Boolean]) 1 else 0
      case x: Double => x.asInstanceOf[Double].toInt
      case x: Long => x.asInstanceOf[Long].toInt
      case x: Float => x.asInstanceOf[Float].toInt
      case x: Byte => x.asInstanceOf[Byte]
      case x: Short => x.asInstanceOf[Short]
      case x: Char => x.asInstanceOf[Char]

    }
  }
}
