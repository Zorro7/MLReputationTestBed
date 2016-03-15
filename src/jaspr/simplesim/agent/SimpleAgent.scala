package jaspr.simplesim.agent

import jaspr.core.Simulation
import jaspr.core.agent.Agent

/**
 * Created by phil on 15/03/16.
 */
class SimpleAgent(override val simulation: Simulation) extends Agent {

  private var currentUtility: Double = 0d
  def utility = currentUtility

  override def tick(): Unit = {
    jaspr.debug("TICK:: ", this)
    currentUtility += 1d
  }
}
