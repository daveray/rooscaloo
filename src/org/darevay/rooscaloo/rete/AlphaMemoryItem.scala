/*
 * AlphaMemoryItem.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete

/**
 * Wrapper object around a fact held in an alpha memory. Maintains list of
 * tokens referencing the fact.
 */
class AlphaMemoryItem(val fact : AnyRef) {

  require(fact != null)

  /**
   * The memories this item is in. An item may be in multiple memories because
   * a fact will be stored once in each alpha memory going up its class
   * hierarchy
   */
  private val memories = new scala.collection.mutable.ArrayBuffer[AlphaMemory]()
  private val tokens = new scala.collection.mutable.ListBuffer[Token]()

  /**
   * The key for the item, i.e. the class of the fact
   */
  def key = fact.getClass

  /**
   * Called when this item is added to an alpha memory
   *
   * @param memory the owning memory
   */
  private [rete] def addedToMemory(memory : AlphaMemory) {
    require(memory != null)
    memories += memory
  }
  
  private [rete] def addToken(token : Token) {
    require(token != null)
    tokens += token
  }

  private [rete] def removeToken(token : Token) {
    require(token != null)
    tokens -= token
  }

  private [rete] def remove() {
    memories.foreach(_ remove this)
    memories.clear()

    while(!tokens.isEmpty)
      tokens.first.delete()
  }

  override def toString = fact.toString
}
