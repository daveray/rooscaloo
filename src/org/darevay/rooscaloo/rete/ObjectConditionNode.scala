/*
 * JoinNode.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete

import org.darevay.rooscaloo.MatchContext

/**
 * A rete node corresponding to a ObjectCondition. Essentially a join node in
 * the usual rete terminology.
 *
 * @param parent the parent beta memory
 * @param alphaMemory the alpha memory this condition draws from
 * @param condition the condition to check.
 * @param bindings map from variable symbol to token offset for this node
 */
class ObjectConditionNode(val parent : BetaMemory,
               val alphaMemory : AlphaMemory,
               val condition : (Token, AnyRef, MatchContext) => Boolean,
               bindings : Map[Symbol, Int])
  extends ConditionNode with BetaMemoryChild with AlphaMemoryListener {

  var childMemoryImpl : BetaMemory = null

  alphaMemory.addListener(this)
  parent.addChildToFront(this)

  override def childMemory = childMemoryImpl

  override def childMemory_=(memory : BetaMemory ) : BetaMemory = {
    require(memory != null)
    
    childMemoryImpl = memory

    alphaMemory.foreach(rightActivate)
    
    memory
  }

  override def buildChildMemory() : BetaMemory = {
    super.buildChildMemory

    childMemory
  }

  override def leftActivate(token : Token) {
    require(token != null)

    alphaMemory.foreach(activate(token, _))
  }

  override def tokenRemoved(token : Token) {
    // Nothing to do
  }

  override def rightActivate(item : AlphaMemoryItem) {
    require(item != null)
    
    parent.foreach(activate(_, item))
  }

  override def toString() = {
    "JoinNode\n" + childMemory
  }

  private def activate(token : Token, item : AlphaMemoryItem) {
    val context = new MatchContext() {
      def apply(name : Symbol) : AnyRef = {
        token.ancestor(bindings(name)).item.get.fact
      }
    }
    if(condition(token, item.fact, context))
      childMemory.leftActivate(token, Some(item))
  }
}
