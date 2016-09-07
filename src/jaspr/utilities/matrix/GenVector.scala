package jaspr.utilities.matrix

/**
  * Created by phil on 28/10/15.
  */

import scala.language.implicitConversions


object GenVector {
  implicit def fromSeq(s: Seq[Double]): GenVector = new GenVector(s)

  implicit def toSeq(c: GenVector): Seq[Double] = c.self

  def apply(values: Double*) = new GenVector(values)
}

class GenVector(val self: Seq[Double]) extends Seq[Double] {

  def @+(a: GenVector): GenVector = {
    (this zip a).map(x => x._1 + x._2)
  }

  def @-(a: GenVector): GenVector = {
    (this zip a).map(x => x._1 - x._2)
  }

  def @*(a: GenVector): GenVector = {
    (this zip a).map(x => x._1 * x._2)
  }

  def @/(a: GenVector): GenVector = {
    (this zip a).map(x => x._1 / x._2)
  }

  def @+(a: Double): GenVector = {
    this.map(_ + a)
  }

  def @-(a: Double): GenVector = {
    this.map(_ - a)
  }

  def @*(a: Double): GenVector = {
    this.map(_ * a)
  }

  def @/(a: Double): GenVector = {
    this.map(_ / a)
  }

  def @^(a: Double): GenVector = {
    this.map(Math.pow(_, a))
  }

  def exp: GenVector = {
    this.map(Math.exp)
  }

  def log: GenVector = {
    this.map(Math.log)
  }

  override def length: Int = self.length

  override def size: Int = self.size

  override def apply(idx: Int): Double = self(idx)

  override def iterator: Iterator[Double] = self.iterator

  override def toString() = {
    self.mkString("[", ",", "]")
  }
}
