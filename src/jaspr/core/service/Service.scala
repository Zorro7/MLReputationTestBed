package jaspr.core.service

import jaspr.core.agent.{Property, Properties}
import jaspr.utilities.NamedEntity

/**
 * Created by phil on 15/03/16.
 */
abstract class Service extends NamedEntity with Properties {

  val request: ServiceRequest

  var start = request.start
  var duration = request.duration
  val serviceContext: ServiceContext

  def end: Int = start + duration

  private var delivered = false
  private var started = false
  def isDelivered = {
    delivered
  }
  def isStarted = {
    started
  }

  def isComplete(currentRound: Int): Boolean
  def canStart(currentRound: Int): Boolean
  def utility(): Double

  def tryEndService(currentRound: Int): Boolean = {
    if (!delivered && isComplete(currentRound)) {
      duration = currentRound - start
      delivered = true
      jaspr.debug("ENDED: ", this)
      true
    } else {
      false
    }
  }

  def tryStartService(currentRound: Int): Boolean = {
    if (!started && canStart(currentRound)) {
      start = currentRound
      started = true
      jaspr.debug("STARTED: ", this)
      true
    } else {
      false
    }
  }

  override def toString: String = {
    super.toString+"["+request.client+","+request.provider+","+start+","+duration+"]"
  }

}
