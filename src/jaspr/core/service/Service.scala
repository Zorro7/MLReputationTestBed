package jaspr.core.service

import jaspr.utilities.NamedEntity

/**
 * Created by phil on 15/03/16.
 */
abstract class Service extends NamedEntity {

  val request: ServiceRequest

  var payload = request.payload
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
  private var _utility: Double = 0d
  def utility(): Double = _utility

  def tryEndService(currentRound: Int): Boolean = {
    if (!delivered && isComplete(currentRound)) {
      duration = currentRound - start
      _utility = request.market.deliver(this)
      delivered = true
      jaspr.debug("ENDED: ", utility(), this)
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
    super.toString+"["+request.client+","+request.provider+","+start+","+duration+","+payload+"]"
  }

}
