package jaspr.utilities.matrix

/**
  * Created by phil on 29/10/15.
  */

import scala.language.implicitConversions


object ColVector {
  implicit def fromSeq(s: Seq[Double]): ColVector = new ColVector(s)

  implicit def toSeq(c: ColVector): Seq[Double] = c.self

  def apply(values: Double*) = new ColVector(values)
}

class ColVector(override val self: Seq[Double]) extends GenVector(self) {

  def transpose(): RowVector = {
    new RowVector(self)
  }

  def @*(that: RowVector): Matrix = {
    new Matrix(for (a <- this) yield {
      new RowVector(for (b <- that) yield {
        a * b
      })
    })
  }

  override def toString() = super.toString + "'"
}
