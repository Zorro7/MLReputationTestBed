package jaspr.core.agent

/**
 * Created by phil on 15/03/16.
 */
trait Properties {

  def properties: Seq[Property]
}

trait AdvertProperties extends Properties {

  def advertProperties: Seq[Property]
}


case class Property(name: String, value: Double) {


}