package jaspr.utilities

/**
  * Created by phil on 14/02/2016.
  */

trait Discretization {

  val upper: Double
  val lower: Double
  val numBins: Int

  lazy val discVals = Range.Double(lower, upper, (upper-lower)/numBins).map(_.toString)

  def discretizeInt(x: Double): Int = {
    discretizeDouble(x).toInt
  }

  def discretizeDouble(x: Double): Double = {
    bound((((x-lower) / (upper - lower)) * numBins).toInt, 0, numBins-1)
  }

  def undiscretize(x: Int): Double = {
    x*(upper - lower)/numBins+lower
  }

  def undiscretize(x: Double): Double = {
    x*(upper - lower)/numBins+lower
  }

  def bound(value: Double, lower: Double, upper: Double): Double = {
    Math.min(upper, Math.max(lower, value))
  }
}
