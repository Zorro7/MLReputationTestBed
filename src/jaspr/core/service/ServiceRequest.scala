package jaspr.core.service

import jaspr.core.agent.{Client, Provider}
import jaspr.utilities.NamedEntity

/**
 * Created by phil on 15/03/16.
 */
class ServiceRequest(val client: Client,
                     val provider: Provider,
                     val start: Int,
                     val duration: Int) extends NamedEntity {

  def end: Int = start + duration

  override def toString: String = {
    super.toString+"["+client+","+provider+","+start+","+duration+"]"
  }
}
