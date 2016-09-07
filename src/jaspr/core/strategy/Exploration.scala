package jaspr.core.strategy

import jaspr.core.service.TrustAssessment
import jaspr.utilities.Chooser

import scala.annotation.tailrec

/**
  * Created by phil on 16/03/16.
  */
trait RecursiveExploration extends Strategy {

  val explorationProbability: Double

  @tailrec
  final def select(orderedAssessments: Seq[TrustAssessment]): TrustAssessment = {
    if (orderedAssessments.size == 1) {
      orderedAssessments.head
    } else {
      if (Chooser.nextDouble() <= explorationProbability) {
        select(orderedAssessments.drop(1))
      } else {
        orderedAssessments.head
      }
    }
  }
}

trait Exploration extends Strategy {

  val explorationProbability: Double

  def select(orderedAssessments: Seq[TrustAssessment]): TrustAssessment = {
    if (orderedAssessments.size == 1) {
      orderedAssessments.head
    } else {
      Chooser.ifHappens(explorationProbability)(
        Chooser.choose(orderedAssessments.drop(1))
      )(
        orderedAssessments.head
      )
    }
  }
}

trait NoExploration extends Strategy {

  def select(orderedAssessments: Seq[TrustAssessment]): TrustAssessment = {
    orderedAssessments.head
  }
}