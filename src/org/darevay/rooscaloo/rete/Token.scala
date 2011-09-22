/*
 * Token.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete


class Token(val node : BetaMemory, 
            val parent : Option[Token],
            val item : Option[AlphaMemoryItem]) {

  require(node != null)
  require(parent != null)
  require(item != null)
  
  private val children = new scala.collection.mutable.ArrayBuffer[Token]()
  private var deletingImpl = false;

  parent.foreach(_ addChild this)
  item.foreach(_ addToken this)

  def ancestor(index : Int) : Token = {
    if(index == 0) this else parent.get.ancestor(index - 1)
  }

  def isDescendantOf(ancestor : Token) : Boolean = {
    parent match {
      case Some(p) if p == ancestor => true
      case Some(p) => p.isDescendantOf(ancestor)
      case None => false
    }
  }

  private def addChild(child : Token) : Unit = children.prepend(child)
  private def removeChild(child : Token) : Unit = children -= child

  private [rete] def deleting = deletingImpl
  
  private [rete] def delete() {
    deletingImpl = true;

    while(!children.isEmpty) {
      children(0).delete()
    }

    node.removeToken(this)

    item.foreach(_ removeToken this)
    parent.foreach(_ removeChild this)
  }
}
