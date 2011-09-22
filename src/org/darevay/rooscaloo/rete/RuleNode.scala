/*
 * RuleNode.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete

import java.lang.IllegalStateException

import org.darevay.rooscaloo.MatchContext
import org.darevay.rooscaloo.MatchInstance
import org.darevay.rooscaloo.Rule

/**
 * Represents a rule node (often called a p-node) in the rete. This is the
 * bottommost node in the network. When it is activated, a rule has matched.
 */
class RuleNode(val rule : Rule,
               val parent : BetaMemory,
               val bindings : Map[Symbol, Int])
  extends BetaMemoryChild {

  /**
   * Map from tokens (received through left activation) to match instantiations.
   * This is necessary to maintain context (bindings, etc) from rule firing to
   * rule retraction.
   */
  val instances = scala.collection.mutable.Map.empty[Token, MatchInstance]

  parent.addChildToFront(this)

  override def leftActivate(token : Token) {
    require(token != null)

    // Create a new context for looking up variable bindings by name
    val context = new MatchContext() {
      def apply(name : Symbol) : AnyRef = {
        token.ancestor(bindings(name)).item.get.fact
      }

      override def toString = bindings.toString
    }

    // Create a match instance and index it by token so we can retrieve it
    // when the rule retracts
    val instance = new MatchInstance(rule, context)
    instances += token -> instance

    rule.fire(instance)
  }

  override def tokenRemoved(token : Token) {
    // Remove the instance from the instance map
    val instance = instances.removeKey(token)

    // Now call the unfire function if there is one.
    instance match {
      case Some(i) => rule.unfire.foreach(_(i))
      case None => throw new IllegalStateException()
    }
  }
}
