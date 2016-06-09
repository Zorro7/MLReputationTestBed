package jaspr.utilities

import scala.util.Random

/**
 * Created by phil on 27/01/16.
 */

object Chooser extends Random {

  // Selects a random value from a list (throws exception IndexOutOfBoundsException if the list is empty)
  def choose[V](items: Seq[V]): V = items(nextInt(items.size))

  def choose[V](items: Seq[V], likelihoods: Seq[Double]): V = {
    assert(items.size == likelihoods.size)
    val pIter = likelihoods.iterator
    val iIter = items.iterator
    var cum = randomDouble(0,likelihoods.sum) - pIter.next()
    while (cum > 0) {
      cum -= pIter.next()
      iIter.next()
    }
    iIter.next()
  }

  def sample[V](all: Seq[V], num: Int): Seq[V] = shuffle(all).take(num)

  def sample[K,V](all: Map[K,V], num: Int): Map[K,V] = {
    shuffle(all.toIterable).take(num).toMap
  }

  // Tests a random number against the given probability, executing ifMet if passed, ifNotMet if not
  def ifHappens[V](probability: Double)(ifMet: => V)(ifNotMet: => V): V =
    if (nextDouble < probability) ifMet else ifNotMet

  // Tests a random number against the given probability, returning Some (ifMet) if passed, None if not
  def getIfHappens[V](probability: Double)(ifMet: => V): Option[V] =
    ifHappens[Option[V]](probability)(Some(ifMet))(None)

  // Selects a random double between a minimum and maximum
  def randomDouble(minimum: Double, maximum: Double) =
    nextDouble * (maximum - minimum) + minimum

  // Selects a random integer between a minimum (inclusive) and maximum (exclusive)
  def randomInt(minimum: Int, maximum: Int) =
    nextInt(maximum - minimum) + minimum

  def bound(value: Double, lower: Double, upper: Double) = {
    Math.min(upper, Math.max(lower, value))
  }
}
