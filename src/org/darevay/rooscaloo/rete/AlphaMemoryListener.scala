/*
 * AlphaMemoryListener.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete

/**
 * Interface implemented by listeners of an alpha memory.
 */
trait AlphaMemoryListener {

  /**
   * Called when an item is added to an alpha memory
   *
   * @param item the item
   */
  def rightActivate(item : AlphaMemoryItem) : Unit
}
