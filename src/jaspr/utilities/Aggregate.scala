package jaspr.utilities

/**
  * Created by phil on 18/01/2017.
  */
class Aggregate(val result: Double = 0, val size: Double = 0) {

  def +(that: Aggregate): Aggregate = {
    new Aggregate(result + that.result, size + that.size)
  }

  def /(that: Aggregate): Aggregate = {
    new Aggregate(result / that.result, size / that.size)
  }

  def *(that: Double): Aggregate = {
    new Aggregate(result * that, size * that)
  }
}