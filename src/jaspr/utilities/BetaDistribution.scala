package jaspr.utilities

import weka.core.Statistics

import scala.math.{max, min, sqrt}

/**
  * Created by phil on 30/06/15.
  */
class BetaDistribution(val alpha: Double = 0, val beta: Double = 0) {
  def +(that: BetaDistribution): BetaDistribution = {
    new BetaDistribution(alpha + that.alpha, beta + that.beta)
  }

  def /(that: BetaDistribution): BetaDistribution = {
    new BetaDistribution(alpha / that.alpha, beta / that.beta)
  }

  def *(that: Double): BetaDistribution = {
    new BetaDistribution(alpha * that, beta * that)
  }

  def integrate(low: Double, high: Double): Double = {
    Statistics.incompleteBeta(alpha, beta, min(1, high)) - Statistics.incompleteBeta(alpha, beta, max(0, low))
  }

  def numEvents(): Double = {
    alpha + beta
  }

  def expected(): Double = {
    alpha / (alpha + beta)
  }

  def belief(): Double = {
    alpha / (alpha + beta + 2)
  }

  def disbelief(): Double = {
    beta / (alpha + beta + 2)
  }

  def uncertainty(): Double = {
    2 / (alpha + beta + 2)
  }

  def numer(): Double = alpha

  def denom(): Double = alpha + beta

  def getBin(nbins: Int): Double = {
    (expected * nbins).floor / nbins
  }

  def isInBin(binStart: Double, nbins: Int): Boolean = {
    //    value >= binStart.getOrElse(value) && value <= binStart.getOrElse(value)+(1d/numBins)
    expected >= binStart && expected <= binStart + (1d / nbins)
  }

  def std(): Double = {
    sqrt(alpha * beta / ((alpha + beta) * (alpha + beta) * (alpha + beta + 1)))
  }

  def getWeighted(area: Double): BetaDistribution = {
    val a_mean = 0.5 + area * (expected - 0.5)
    val a_std = 0.288675 + area * (std - 0.288675)
    new BetaDistribution(
      (((a_mean * a_mean) - (a_mean * a_mean * a_mean)) / (a_std * a_std)) - a_mean, // updated alpha
      ((((1 - a_mean) * (1 - a_mean)) - ((1 - a_mean) * (1 - a_mean) * (1 - a_mean))) / (a_std * a_std)) - (1 - a_mean) // updated beta
    )
  }

  override def toString = "Beta[" + alpha + "," + beta + "]=" + expected
}
