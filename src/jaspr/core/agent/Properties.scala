package jaspr.core.agent

/**
 * Created by phil on 15/03/16.
 */
trait Properties {

  def properties: Map[String, Property]
}

trait AdvertProperties extends Properties {

  def advertProperties: Map[String, Property]
}


case class Property(name: String) {

}