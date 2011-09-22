/*
 * MatchInstanceKey.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

/**
 * Represents a rule instantiation, i.e. a rule that has matched along with
 * all of the variable bindings for that rule. This object is made available
 * when a rule fires and when it retracts.
 *
 * @param rule the rule that matched
 * @param context the match context for access to variable bindings
 */
class MatchInstance(val rule : Rule,
                    val context : MatchContext) {

  override def toString = {
    val ss = super.toString
    val addr = ss.drop(ss.indexOf('@'))
    "[Rule: " + rule.name + ", " + addr + "]"
  }
}
