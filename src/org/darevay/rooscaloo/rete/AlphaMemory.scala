/*
 * AlphaMemory.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete

/**
 * An alpha memory for a particular type of object.
 *
 * <p>Alpha memories form a hierarchy parallel to the Scala class hierarchy of
 * facts inserted in the rete. When a fact is added to an alpha memory, it is
 * automatically added to all parent memories as well to ensure that tests for
 * parent classes will get that object as well.
 */
class AlphaMemory (val key : java.lang.Class[_],
                   val parent : Option[AlphaMemory]) {
  require(key != null)

  // Use list buffers because items will frequently be added and removed from
  // the list. ???
  private var items = List[AlphaMemoryItem]()
  private var listeners = List[AlphaMemoryListener]()

  def addListener(listener : AlphaMemoryListener) {
    listeners = listener :: listeners
  }

  /**
   * Add the given item to the alpha memory, i.e. "activate" the memory
   *
   * @param item the new item
   */
  def add(item : AlphaMemoryItem) {
    require(item != null)

    //println("Activate " + this + " with " + item)

    items = item :: items
    item.addedToMemory(this)

    listeners.foreach(_ rightActivate item)
    parent.foreach(_ add item)
  }

  /**
   * Remove the given item from this alpha memory
   *
   * @param item the item to remove
   */
  def remove(item : AlphaMemoryItem) {
    items -= item
  }

  /**
   * Apply the given operation for each item in the memory
   *
   * @param op the operation
   */
  def foreach(op : (AlphaMemoryItem) => Unit) {
    items.foreach(op)
  }

  override def toString = "AlphaMemory[" + key.getName + "], " + listeners.size + " listeners, parent = " + parent
}
