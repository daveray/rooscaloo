/*
 * NotCondition.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

case class NotCondition(subs: List[Condition]) extends Condition(Set()) {
  
}

