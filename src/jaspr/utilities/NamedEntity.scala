package jaspr.utilities

import scala.collection.immutable.{SortedMap, TreeMap}
import scala.collection.mutable
import scala.language.implicitConversions

/**
  * Created by phil on 26/01/16.
  */

object NamedEntity {

  val indexes = new mutable.HashMap[Class[_ <: NamedEntity], Int]

  def nextIndex(ne: NamedEntity): Int = {
    val x = indexes.getOrElse(ne.getClass, 0) + 1
    indexes.put(ne.getClass, x)
    x
  }

  implicit def toMap[T <: NamedEntity](nes: List[T]): Map[String, T] = {
    nes.map(x => x.name -> x).toMap
  }

  implicit def toSortedMap[T <: NamedEntity](nes: List[T]): SortedMap[String, T] = {
    TreeMap[String, T](nes.map(x => x.name -> x): _*)
  }
}


trait NamedEntity {

  val id: Int = NamedEntity.nextIndex(this)
  val name: String = id.toString

  override def toString: String = {
    this.getClass.getSimpleName + name
  }
}
