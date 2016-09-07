package jaspr.core.agent

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 25/05/16.
  */
trait Preferences {

  def preferences: SortedMap[String, Property]

  def preference(key: String): Property = {
    preferences.get(key).get
  }

}
