package jaspr.sellerssim.dynamicsimulation

import jaspr.core.agent.{Client, Provider}
import jaspr.core.service.{ClientContext, ServiceRequest}
import jaspr.sellerssim.SellerNetwork
import jaspr.sellerssim.agent.{Buyer, Seller}
import jaspr.utilities.{Chooser, Tickable}

/**
  * Created by phil on 15/03/16.
  */
class DynamicSellerNetwork(override val simulation: DynamicSellerSimulation) extends SellerNetwork with Tickable {

  val config: DynamicSellerConfiguration = simulation.config.asInstanceOf[DynamicSellerConfiguration]

  private var _clients: Seq[Buyer] = List.fill(simulation.config.numClients)(
    new Buyer(simulation)
  )
  private var _providers: Seq[Seller] = List.fill(simulation.config.numProviders)(
    new Seller(simulation)
  )
//  private var groups: Map[Client,Seq[Provider]] = {
//    clients.map(x => x -> Chooser.sample(providers, (simulation.config.numProviders*0.2).toInt)).toMap
//  }
//
//  private var departedClients: List[Buyer] = Nil
//  private var departedProviders: List[Seller] = Nil

  override def utility(): Double = clients.map(_.utility).sum //+ departedClients.map(_.utility).sum

  override def clients: Seq[Buyer] = _clients

  override def providers: Seq[Seller] = _providers


  override def possibleRequests(context: ClientContext): Seq[ServiceRequest] = {
    val requests = providers.withFilter(x =>
      x.capableOf(context.payload, 0) && Chooser.nextDouble() < simulation.config.providerAvailabilityLikelihood
    ).map(x =>
      new ServiceRequest(
        context.client, x, simulation.round, 0, context.payload, context.market
      )
    )
    if (requests.isEmpty) possibleRequests(context)
    else requests
  }

  override def tick(): Unit = {
    jaspr.debug(100, "tick", simulation.round)
//    _clients = clients.map(x =>
//      Chooser.ifHappens(simulation.config.clientAttrition)({
//        departedClients = x :: departedClients
//        new Buyer(simulation)
//      })(
//        x
//      )
//    )
//    _providers = providers.map(x =>
//      Chooser.ifHappens(simulation.config.providerAttrition)({
//        departedProviders = x :: departedProviders
//        new Seller(simulation)
//      })(
//        x
//      )
//    )
    if (simulation.config.networkTickInterval > 0 && simulation.round % simulation.config.networkTickInterval == 0) {
      _providers =
        Chooser.sample(providers, ((1 - simulation.config.providerAttrition) * simulation.config.numProviders).toInt) ++
          List.fill((simulation.config.providerAttrition * simulation.config.numProviders).toInt)(
            new Seller(simulation)
          )
    }
//    if (simulation.round % simulation.config.networkTickInterval == 0)
//      groups = clients.map(x => x -> Chooser.sample(providers, (simulation.config.numProviders*1d).toInt)).toMap

//    println(_providers.size, departedProviders.size)
  }
}
