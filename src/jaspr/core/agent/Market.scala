package jaspr.core.agent

import jaspr.core.service.Service
import jaspr.core.simulation.Simulation

/**
 * Created by phil on 17/03/16.
 */
trait Market {
  def deliver(service: Service): Double
}
