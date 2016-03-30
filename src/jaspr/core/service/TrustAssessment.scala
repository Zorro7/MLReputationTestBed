package jaspr.core.service

import java.text.DecimalFormat

import jaspr.utilities.NamedEntity

/**
 * Created by phil on 15/03/16.
 */
class TrustAssessment(val request: ServiceRequest, val trustValue: Double) extends NamedEntity {

  val df = new DecimalFormat("#.##")
  override def toString: String = request.flatten().map(_.provider.properties.values.map(x => df.format(x.doubleValue))).toString()+": "+trustValue
}
