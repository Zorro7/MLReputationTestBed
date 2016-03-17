package jaspr.core.service

import jaspr.core.agent.{Market, Properties, Property, Client}
import jaspr.utilities.NamedEntity

/**
 * Created by phil on 15/03/16.
 */
class ClientContext(val client: Client,
                    val round: Int,
                    val market: Market,
                    override val properties: Map[String,Property]
                     ) extends NamedEntity with Properties
