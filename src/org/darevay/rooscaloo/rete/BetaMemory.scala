/*
 * BetaMemory.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete

import scala.collection.mutable.ListBuffer

class BetaMemory(val parent : ConditionNode) {
  
  private val childrenImpl = new ListBuffer[BetaMemoryChild]()
  private val tokens = new ListBuffer[Token]()

  def addChildToFront(child : BetaMemoryChild) = {
    childrenImpl.prepend(child)
  }

  def addChildToBack(child : BetaMemoryChild) = {
    childrenImpl.append(child)
  }

  def removeChild(child : BetaMemoryChild) = {
    childrenImpl -= child
  }

  def children : List[BetaMemoryChild] = childrenImpl.toList
  
  private [rete] def newToken(parent : Option[Token], item : Option[AlphaMemoryItem]) : Token = {
    val token = new Token(this, parent, item)
    tokens += token
    token
  }

  private [rete] def removeToken(token : Token) = {
    require(token != null)
    require(token.node == this)

    tokens -= token
    children.foreach(_.tokenRemoved(token))
  }

  def leftActivate(token : Token, item : Option[AlphaMemoryItem]) : Token = {
    require(token != null)
    require(item != null)


    val nt = newToken(Some(token), item)
    children.foreach(_.leftActivate(nt))
    nt
  }

  def updateNewChildWithMatches(child : BetaMemoryChild) : BetaMemoryChild = {
    foreach(child.leftActivate(_))
    child
  }

  def foreach(op : (Token) => Unit) {
    tokens.foreach(op)
  }

  def forall(op : Token => Boolean) : Boolean = {
    tokens.forall(op)
  }

  override def toString() = {
    "Beta\n   " + children.mkString("   \n")
  }
}
