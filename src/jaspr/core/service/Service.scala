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

  def isDelivered = {
    request.isDelivered
  }

  def isStarted = {
    request.isStarted
  }

  def isComplete(currentRound: Int): Boolean

  def canStart(currentRound: Int): Boolean

  def dependenciesSatisfied: Boolean

  private var _utility: Double = 0d

  def utility(): Double = _utility

  def tryEndService(currentRound: Int): Boolean = {
    if (request.tryEndService(this, currentRound)) {
      duration = currentRound - start
      _utility = request.market.deliver(this)
      jaspr.debug("ENDED: ", utility(), this)
      true
    } else {
      false
    }
  }

  def tryStartService(currentRound: Int): Boolean = {
    if (request.tryStartService(this, currentRound)) {
      start = currentRound
      jaspr.debug("STARTED: ", this)
      true
    } else {
      false
    }
  }

  override def toString: String = {
    super.toString + "[" + request.client + "," + request.provider + "," + start + "," + duration + "," + payload + "]"
  }

}
