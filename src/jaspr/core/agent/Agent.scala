package jaspr.core.agent

import jaspr.core.provenance.Provenance
import jaspr.core.simulation.{Configuration, Simulation}
import jaspr.utilities.{NamedEntity, Tickable}

/**
  * Created by phil on 26/01/16.
  */
abstract class Agent extends NamedEntity with Tickable with Provenance {

  def utility: Double

  /** simulation that this agent is in */
  val simulation: Simulation
  val config: Configuration = simulation.config
}
