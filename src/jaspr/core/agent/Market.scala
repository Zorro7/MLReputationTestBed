package jaspr.core.agent

import jaspr.core.service.Service

/**
  * Created by phil on 17/03/16.
  */
trait Market {
  def deliver(service: Service): Double
}
