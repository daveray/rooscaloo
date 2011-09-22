/*
 * NotNode.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete

/**
 * A condition node that models a negated condition(s) in the rete network
 *
 * @param parent the parent beta memory
 * @param bottomOfSubnet the bottom of the negated subnetwork. This beta memory
 *     will share a common ancestor with this node
 */
class NotNode(val parent : BetaMemory,
              val bottomOfSubnet : BetaMemory)
  extends ConditionNode with BetaMemoryChild {

  private val tokenMap = scala.collection.mutable.Map.empty[Token, Token]
  private var childMemoryImpl : BetaMemory = null
  
  parent.addChildToBack(this)

  // Construct an internal listener to handle tokens coming out of the bottom
  // of the negated subnet
  val subnetChild = new BetaMemoryChild() {
    override def leftActivate(token : Token) = leftActivateFromSubnet(token)
    override def tokenRemoved(token : Token) = tokenRemovedFromSubnet(token)
  }
  bottomOfSubnet.addChildToFront(subnetChild)

  override def childMemory = childMemoryImpl

  override def childMemory_=(memory : BetaMemory ) = {
    require(memory != null)
    childMemoryImpl = memory
    parent.foreach(leftActivate)
    memory
  }

  override def leftActivate(token : Token) {
    require(token != null)

    // When a token comes in from the left we need to check whether the negated
    // subnet has a match. To do this we check whether there are any tokens
    // on the right with this token as a parent. If there are none, then the
    // negated conditions are false and thus, this test passes.
    if(bottomOfSubnet.forall(!_.isDescendantOf(token))) {
      // Remember the new token so we can handle retraction on the subnet
      val newToken = childMemory.leftActivate(token, None)
      tokenMap.put(token, newToken)
    }
  }

  override def tokenRemoved(token : Token) {
    // If a token is removed from the left, then we only need to remove it
    // from the map. We really only care if a token is removed from the bottom
    // of the subnet
    tokenMap.removeKey(token)
  }

  /**
   * Calculate a common ancestor for the given token (from the subnet) and
   * this node.
   */
  private def commonAncestor(token : Token) : Token = {
    require(token != null)

    if(token.node == parent) token
    else commonAncestor(token.parent.get)
  }

  private def leftActivateFromSubnet(token : Token) {
    require(token != null)

    // If we get a new token from the subnet, that means that the sbunet part
    // is true, so the overall test is false. Thus, we need to suppress any
    // tokens we previously let through from the left
    val ca = commonAncestor(token)
    assert(ca != null)
    
    // Retrieve the token previously added to the token map and delete it. This
    // will cause cascading retractions in sub-nodes...
    tokenMap.removeKey(ca).foreach(_ delete)
  }

  private def tokenRemovedFromSubnet(token : Token) {
    require(token != null)

    // When a token is removed from the subnet, then the subnet condition has become
    // false which means the that overall not condition is true. We need to
    // reactivate any tokens from the left that were previously suppressed
    // in leftActivateFromSubnet
    val ca = commonAncestor(token);

    // The token may be in the middle of deleting itself (causing the subnet
    // retraction). In this case we ignore it.
    if(!ca.deleting)
        leftActivate(ca)
  }

  override def toString() = {
    "NotNode\n" + childMemory
  }
}
