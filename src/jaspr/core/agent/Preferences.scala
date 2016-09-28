package jaspr.core.agent

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 25/05/16.
  */
trait Preferences {

  def preferences: SortedMap[String, FixedProperty]

  def preference(key: String): FixedProperty = {
    preferences.get(key).get
  }

}
