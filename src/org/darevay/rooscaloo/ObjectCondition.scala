/*
 * ObjectCondition.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

import org.darevay.rooscaloo.util.Util._

object ObjectCondition {

  def apply[T <: AnyRef](klass : Class[T],
            binding : Option[Symbol],
            condition : (T, MatchContext) => Boolean) : ObjectCondition[T] = new ObjectCondition(klass, binding, condition)

  def apply[T <: AnyRef](klass : Class[T],
           condition : (T, MatchContext) => Boolean): ObjectCondition[T] = apply(klass, None, condition)

}

/**
 * A condition that tests for a particular type of object and performs some
 * additional test on it.
 *
 * @param klass the type of object to test for
 * @param condition the predicate to test on the object. Includes a context
 *    parameter for access to previously bound variables
 * @param binding optional variable to bind the tested object to
 */
class ObjectCondition[T <: AnyRef](val klass : Class[T],
                                   val binding : Option[Symbol],
                                   val condition : (T, MatchContext) => Boolean) extends Condition(binding toSet) {


  def apply(fact : AnyRef, context : MatchContext) : Boolean = {
    if(klass.isInstance(fact)) condition(klass.cast(fact), context) else false
  }
}
