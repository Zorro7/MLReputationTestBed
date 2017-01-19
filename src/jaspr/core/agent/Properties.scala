package jaspr.core.agent

import jaspr.core.service.Payload
import jaspr.utilities.{Chooser, NamedEntity}

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

  def adverts: SortedMap[String, Property]

  def advert(key: String): Property = {
    adverts(key)
  }
}

abstract class Property() extends NamedEntity {

  def value: AnyVal
  def sample: FixedProperty = FixedProperty(name, value)

  def booleanValue: Boolean = {
    doubleValue > 0
  }

  def doubleValue: Double = {
    value match {
      case x: Double => x.asInstanceOf[Double]
      case x: Int => x
      case x: Boolean => if (x) 1d else 0d
      case x: Long => x
      case x: Float => x
      case x: Byte => x
      case x: Short => x
      case x: Char => x
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

  override def toString: String = {
    super.toString + "-" + mean+","+std
  }

  override def value: Double = {
    Chooser.nextGaussian()*std + mean
  }
}