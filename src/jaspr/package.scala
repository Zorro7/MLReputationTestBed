import jaspr.core.Configuration

/**
 * Created by phil on 26/01/16.
 */
package object jaspr {

  val debugLevel = Configuration.debug

  def debug(str: String, objs: Any*): Unit = {
    debug(5, str, objs:_*)
  }

  // Coded like this to allow a message followed by other strings/objects.
  // This avoids calling toString on everything when debug is disabled.
  def debug(level: Int, str: String, objs: Any*): Unit = {
    if (level > debugLevel) println(objs.mkString(str, " :: ", ""))
  }

}
