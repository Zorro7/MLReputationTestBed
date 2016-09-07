package jaspr.utilities.matrix

/**
  * Created by phil on 28/10/15.
  */

import scala.language.implicitConversions


object RowVector {
  implicit def fromSeq(s: Seq[Double]): RowVector = new RowVector(s)

  implicit def toSeq(c: RowVector): Seq[Double] = c.self

  def apply(values: Double*) = new RowVector(values)
}

class RowVector(override val self: Seq[Double]) extends GenVector(self) {

  def transpose(): ColVector = {
    new ColVector(self)
  }

  def @*(a: ColVector): Double = {
    (this zip a).map(x => x._1 * x._2).sum
  }
}
