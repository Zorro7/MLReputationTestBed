package jaspr.core.service

import jaspr.core.agent._
import jaspr.utilities.NamedEntity

import scala.annotation.tailrec
import scala.collection.immutable.SortedMap

/**
  * Created by phil on 15/03/16.
  */


class ServiceRequest(val client: Client,
                     val provider: Provider,
                     val start: Int,
                     val duration: Int,
                     val payload: Payload,
                     val market: Market,
                     override val properties: SortedMap[String,Property] = Nil,
                     val dependencies: Seq[ServiceRequest] = Nil
                    ) extends NamedEntity with Properties {

  def flatten(): Seq[ServiceRequest] = {
    @tailrec
    def flatten(curr: List[ServiceRequest], acc: List[ServiceRequest]): Seq[ServiceRequest] = {
      if (curr.isEmpty) acc
      else flatten(curr.tail ++ curr.head.dependencies, curr.head :: acc)
    }
    flatten(this :: Nil, Nil)
  }

  def end: Int = start + duration

  private var delivered = false
  private var started = false

  def isDelivered = {
    delivered
  }

  def isStarted = {
    started
  }

  def tryEndService(service: Service, currentRound: Int): Boolean = {
    if (!delivered && service.isComplete(currentRound)) {
      delivered = true
      true
    } else {
      false
    }
  }

  def tryStartService(service: Service, currentRound: Int) = {
    if (!started && service.canStart(currentRound)) {
      started = true
      true
    } else {
      false
    }
  }

  override def toString: String = {
    super.toString + "[" + client + "," + provider + "," + start + "," + duration + "," + payload + "]"
  }
}
