package jaspr.core.agent

/**
 * Created by phil on 25/05/16.
 */
trait Preferences {

  def preferences: Map[String,Property]

  def preference(key: String): Property = {
    preferences.get(key).get
  }

}
