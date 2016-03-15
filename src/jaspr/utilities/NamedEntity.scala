package jaspr.utilities

import scala.collection.mutable

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
}


trait NamedEntity {

  val id: Int = NamedEntity.nextIndex(this)
  val name: String = id.toString

  override def toString: String = {
    this.getClass.getSimpleName + name
  }
}
