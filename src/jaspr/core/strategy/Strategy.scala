package jaspr.core.strategy

import jaspr.core.Network
import jaspr.core.service.{ServiceRequest, TrustAssessment, ClientContext}
import jaspr.utilities.{ArgumentUtils, Chooser}
import org.apache.commons.beanutils.ConstructorUtils

/**
 * Created by phil on 16/03/16.
 */

object Strategy {
  /**
    *
    * @param name The strategy to instantiate with full path and contructor arguments separated by a ';'.
    * @return
    */
  def forName(name: String): Strategy = {
    if (name.contains("(")) {
      val sname = name.substring(0, name.indexOf("("))
      val sargs = name.substring(name.indexOf("(") + 1, name.indexOf(")")).split(";").toList
      ConstructorUtils.invokeConstructor(Class.forName(sname), ArgumentUtils.convargs(sargs).toArray).asInstanceOf[Strategy]
    } else {
      Class.forName(name).newInstance().asInstanceOf[Strategy]
    }
  }
}

abstract class Strategy {

  var initTime: Long = 0
  var computeTime: Long = 0
  var computeProviderTime: Long = 0
  var callCounter: Int = 0
  var callProviderCounter: Int = 0

  val name: String = this.getClass.getSimpleName
  override def toString = name

  def initStrategy(network: Network, context: ClientContext): StrategyInit
  def computeAssessment(init: StrategyInit, request: ServiceRequest): TrustAssessment

  def select(orderedAssessments: Seq[TrustAssessment]): TrustAssessment

  def assessReputation(network: Network, context: ClientContext): TrustAssessment = {
    val requests = network.possibleRequests(context)
    val initStart = System.currentTimeMillis()
    val init = initStrategy(network, context)
    val initEnd = System.currentTimeMillis()
    val computeStart = System.currentTimeMillis()
    val orderedProviders = rank(init, requests)
    val computeEnd = System.currentTimeMillis()
    initTime += initEnd - initStart
    computeTime += computeEnd - computeStart
    computeProviderTime += (computeEnd - computeStart)/orderedProviders.size
    callCounter += 1
    callProviderCounter += orderedProviders.size
    select(orderedProviders)
  }

  def rank(init: StrategyInit, requests: Seq[ServiceRequest]): Seq[TrustAssessment] = {
    val assessments = requests.map(computeAssessment(init, _))
    Chooser.shuffle(assessments).sortBy(x =>
      if (x.trustValue.isNaN) Double.MinValue else x.trustValue
    ).reverse
  }

  def resetTimeCounters(): Unit = {
    initTime = 0
    computeTime = 0
    computeProviderTime = 0
    callCounter = 0
    callProviderCounter = 0
  }
}
