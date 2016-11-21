package jaspr.sellerssim

import jaspr.core.agent._
import jaspr.core.service.ClientContext
import jaspr.core.simulation.{Configuration, Network, NetworkMarket}
import jaspr.sellerssim.agent.{Witness, WitnessModel}
import jaspr.sellerssim.service.ProductPayload

import scala.collection.immutable.SortedMap

/**
  * Created by phil on 07/09/16.
  */
abstract class SellerConfiguration extends Configuration {

  def clientInvolvementLikelihood: Double

  def numClients: Int

  def numProviders: Int

  override val numAgents = numClients + numProviders

  def witnessesAvailable: Double

  val simcapabilities: Seq[ProductPayload]

  def baseUtility: Double

  def eventLikelihood: Double

  def eventEffects: Double

  def memoryLimit: Int

  def properties(agent: Agent): SortedMap[String, FixedProperty]

  def preferences(agent: Client): SortedMap[String, FixedProperty]

  def capabilities(agent: Provider): Seq[ProductPayload]

  def adverts(agent: Agent with Properties): SortedMap[String, FixedProperty]
  def adverts(payload: ProductPayload, agent: Agent with Properties): SortedMap[String, FixedProperty]

  def clientContext(network: Network with NetworkMarket, agent: Client with Preferences, round: Int): ClientContext

  def witnessModel(witness: Witness, network: Network): WitnessModel

  def network(simulation: SellerSimulation): SellerNetwork
}
