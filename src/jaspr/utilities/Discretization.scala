package jaspr.utilities

/**
  * Created by phil on 14/02/2016.
  */
trait Discretization {

  val upper: Double
  val lower: Double
  val numBins: Int

  lazy val discVals = Range.Double.inclusive(lower, upper, (upper-lower)/numBins).map(_.toString)

  def discretizeInt(x: Double): Int = {
    val b = (((x-lower) / (upper - lower)) * numBins).toInt
    if (b <= 0) 0
    else if (b >= numBins) numBins
    else b
  }

  def discretizeDouble(x: Double): Double = {
    val b = (((x-lower) / (upper - lower)) * numBins).toInt
    if (b >= numBins) numBins
    else b
  }

  def bound(value: Double, lower: Double, upper: Double) = {
    Math.min(upper, Math.max(lower, value))
  }
}
