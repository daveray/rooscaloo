/*
 * RuleBuilder.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

object RuleBuilder {

  implicit def classToObjectCondition[T <: AnyRef](klass : Class[T]) : ObjectConditionBuilder[T] = {
      new ObjectConditionBuilder(klass)
  }

  class ObjectConditionBuilder[T <: AnyRef](private var klass : Class[T]) {
    private var binding : Option[Symbol] = None

    def as(s : Symbol) : ObjectConditionBuilder[T] = {
      binding = Some(s)
      this
    }

    def where(condition : (T, MatchContext) => Boolean) : ObjectCondition[T] = {
      ObjectCondition(klass, binding, condition)
    }
  }

  implicit def listToNotBuilder(subs : List[Condition]) : NotConditionBuilder = {
    new NotConditionBuilder(subs)
  }

  class NotConditionBuilder(val subs: List[Condition]){

    def unary_-() : NotCondition = { NotCondition(subs) }
  }
}
