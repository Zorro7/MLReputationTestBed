package jaspr.utilities

import jaspr.utilities.matrix.RowVector

import scala.language.implicitConversions
import weka.core.{RandomVariates, Statistics}

object Dirichlet {
  def lnbeta(x: RowVector): Double = {
    x.map(Statistics.lnGamma).sum - Statistics.lnGamma(x.sum)
  }

  val rand: RandomVariates = new RandomVariates(); // alpha, lambda, engine.

  def apply(alphaVals: Double*) = {
    new Dirichlet(alphaVals)
  }
}

class Dirichlet(val alpha: RowVector, val domain: RowVector) {
  assert(alpha.size == domain.size)

  import Dirichlet._

  def this(alpha: Seq[Double]) = this(alpha, Range.Double(0, alpha.size, 1))
  def this(dim: Int, value: Double) = this(Seq.fill(dim)(value))
  def this(dim: Int) = this(dim, 1d)

  def mean(): RowVector = {
    val sum = alpha.sum
    alpha.map(_ / sum)
  }

  def expval(funch: RowVector=>RowVector = x=>x) = {
    (mean @* funch(domain)).sum
  }

  def expvar(funch: RowVector=>RowVector = x=>x) = {
    (mean @* ((funch(domain) @^ 2) @- expval(funch))).sum
  }

  def stderr(funch: RowVector=>RowVector = x=>x) = {
    Math.sqrt(expvar(funch))
  }

  def logweight(that: Dirichlet): Double = {
    lnbeta(this.alpha @+ that.alpha @+ 1) - lnbeta(this.alpha) - lnbeta(that.alpha)
  }

  def observe(data: Seq[Double]): Dirichlet = { // this method may be inefficient?
    val tmp = (alpha zip domain).map(x=> (x._1+data.count(_ == x._2), x._2)).unzip
    new Dirichlet(tmp._1, tmp._2)
  }

  def marginalLogLikelihood(data: Seq[Double]) = {
    lnbeta(observe(data).alpha) - lnbeta(this.alpha)
  }

//  def sample(): RowVector = {
//    alpha.map(rand.nextGamma) // alpha, lambda = 1 TODO check
//  }

//  def sample(n: Int): Seq[RowVector] = {
//    for (i <- 1 to n) yield sample()
//  }


  def size = {
    alpha.size
  }

  override def toString(): String = {
    alpha+", "+domain
  }
}
