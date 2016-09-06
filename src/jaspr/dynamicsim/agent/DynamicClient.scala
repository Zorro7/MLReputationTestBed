package jaspr.dynamicsim.agent

import jaspr.core.Simulation
import jaspr.core.agent.{Client, Property}
import jaspr.core.provenance.{Provenance, Record}
import jaspr.core.service.{ClientContext, Service, TrustAssessment}
import jaspr.dynamicsim.DynamicSimulation

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 06/09/16.
  */
class DynamicClient(override val simulation: DynamicSimulation) extends Client {
  override def generateContext(): ClientContext = ???

  override def generateComposition(context: ClientContext): TrustAssessment = ???

  override def makeRequest(assessment: TrustAssessment): Unit = ???

  override def receiveService(service: Service): Unit = ???

  override def preferences: SortedMap[String, Property] = ???

  override def utility: Double = ???

  override val memoryLimit: Int = simulation.config.memoryLimit

  override def getProvenance[T <: Record](agent: Provenance): Seq[T] = ???
}
