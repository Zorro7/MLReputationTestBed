
/**
  * Created by phil on 26/01/16.
  */
package object jaspr {

  val parallel = false
  val debugLevel = 500

  def debug(str: String, objs: Any*): Unit = {
    debug(5, str, objs: _*)
  }

  def debug(level: Int, str: String, objs: Any*): Unit = {
    if (level > debugLevel) println(objs.mkString(str, " :: ", ""))
  }
}
