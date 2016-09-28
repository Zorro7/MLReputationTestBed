package jaspr.core.agent

import jaspr.core.service.Payload
import jaspr.utilities.NamedEntity

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 15/03/16.
  */

trait Properties {

  def properties: SortedMap[String, Property]

  def property(key: String): Property = {
    properties(key)
  }

  //  val df = new DecimalFormat("0.00")
  //  override def toString: String = super.toString+":"+properties.values.map(x => df.format(x.doubleValue)).toString
}

trait AdvertProperties extends Properties {

  def advertProperties: SortedMap[String, Property]

  def advert(key: String): Property = {
    advertProperties(key)
  }

  def payloadAdverts(payload: Payload): SortedMap[String,Property]
}

abstract class Property() extends NamedEntity {

  def value: AnyVal

  def booleanValue: Boolean = {
    doubleValue > 0
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
    doubleValue.toInt
  }

  override def toString: String = {
    super.toString + "-" + value
  }
}

case class FixedProperty(override val name: String, override val value: AnyVal) extends Property


case class GaussianProperty(override val name: String, mean: Double, std: Double) extends Property {

  override def value: Double = {
    Math.random()*std + mean
  }
}