/*
 * Condition.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

/**
 * Base class for conditions in a rule
 *
 * @param binds set of variables bound by this condition
 * @param uses set of variables used by this condition
 */
class Condition(val binds : Set[Symbol]) {

  /**
   * @return the negation of this condition
   */
  def unary_-() : NotCondition = {
    NotCondition(List(this))
  }
}
