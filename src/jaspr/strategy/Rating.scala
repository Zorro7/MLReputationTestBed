package jaspr.strategy

import jaspr.core.agent.{Client, Provider}

/**
  * Created by phil on 16/03/16.
  */


class Rating(val client: Client, val provider: Provider, val round: Int, val rating: Double) {

  def success: Boolean = rating > 0d
}
