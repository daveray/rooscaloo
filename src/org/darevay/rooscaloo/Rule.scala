/*
 * Rule.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

object Rule {

  def apply(name : String,
            conditions : List[Condition],
            fire : (MatchInstance) => Unit,
            unfire : Option[(MatchInstance) => Unit] ) : Rule = {
    new Rule(name, conditions, fire, unfire)
  }
}

class Rule(val name : String, 
           val conditions : List[Condition],
           val fire : (MatchInstance) => Unit,
           val unfire : Option[(MatchInstance) => Unit]) {

}
