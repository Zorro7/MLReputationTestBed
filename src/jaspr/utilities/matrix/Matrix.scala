package jaspr.utilities.matrix

import scala.language.implicitConversions

/**
  * Created by phil on 29/10/15.
  */
object Matrix {
  //  implicit def fromSeq(s: Seq[GenVector]): Matrix = new Matrix(s)
  //  implicit def toSeq(c: Matrix): Seq[GenVector] = c.self

  implicit def fromSeq(s: Seq[Seq[Double]]): Matrix = new Matrix(s.map(new RowVector(_)))

  implicit def toSeq(s: Matrix): Seq[Seq[Double]] = s.self

  def apply(values: GenVector*) = new Matrix(values)
}

class Matrix(val self: Seq[GenVector]) extends Seq[GenVector] {

  //  def this(values: Seq[Seq[Double]]) = this(values.map(new RowVector(_)))
  def this(d1: Int, d2: Int, value: Double) = this(Seq.fill[Double](d1, d2)(value).map(new RowVector(_)))


  def @+(a: Matrix): Matrix = {
    (this zip a).map(x => x._1 @+ x._2)
  }

  def @-(a: Matrix): Matrix = {
    (this zip a).map(x => x._1 @- x._2)
  }

  def @*(a: Matrix): Matrix = {
    (this zip a).map(x => x._1 @* x._2)
  }

  def @/(a: Matrix): Matrix = {
    (this zip a).map(x => x._1 @/ x._2)
  }

  def @/(a: ColVector): Matrix = {
    this.cols.map(x => x @/ a).transpose // transpose cause the default representation for Matrix is in rows
  }

  def @/(a: RowVector): Matrix = {
    this.rows.map(x => x @/ a)
  }

  def @*(a: ColVector): Matrix = {
    this.cols.map(x => x @* a).transpose // transpose cause the default representation for Matrix is in rows
  }

  def @*(a: RowVector): Matrix = {
    this.rows.map(x => x @* a)
  }


  def @+(a: Double): Matrix = {
    this.map(_ @+ a)
  }

  def @-(a: Double): Matrix = {
    this.map(_ @- a)
  }

  def @*(a: Double): Matrix = {
    this.map(_ @* a)
  }

  def @/(a: Double): Matrix = {
    this.map(_ @/ a)
  }

  def @^(a: Double): Matrix = {
    this.map(_ @^ a)
  }

  def exp: Matrix = {
    this.map(_.exp)
  }

  def log: Matrix = {
    this.map(_.log)
  }

  private def rows: Seq[RowVector] = self.map(new RowVector(_))

  private def cols: Seq[ColVector] = self.transpose.map(new ColVector(_))

  def colsum(): RowVector = {
    this.reduce(_ @+ _)
  }

  def rowsum(): ColVector = {
    this.map(_.sum)
  }

  override def length: Int = self.length

  override def size: Int = self.size

  override def apply(idx: Int): GenVector = self(idx)

  override def iterator: Iterator[GenVector] = self.iterator

  override def toString() = {
    self.mkString("[\t", "\n\t", "\n]")
  }
}
