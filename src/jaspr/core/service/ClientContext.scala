package jaspr.core.service

import jaspr.core.agent.{Market, Client}
import jaspr.utilities.NamedEntity

/**
 * Created by phil on 15/03/16.
 */
class ClientContext(val client: Client,
                    val round: Int,
                    val payload: Payload,
                    val market: Market
                     ) extends NamedEntity
