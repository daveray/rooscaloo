/*
 * BetaMemoryChild.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete

/**
 * Interface implemented by children of beta memories.
 */
trait BetaMemoryChild {

  /**
   * Called when a token is added to the beta memory. This means that the token
   * has passed the condition above the beta memory and should now be tested
   * by the child receiving the call.
   *
   * @param token the token
   */
  def leftActivate(token : Token) : Unit

  /**
   * Called when a token previously passed to {@link #leftActivate(Token)} is
   * removed because a higher condition has stopped matching.
   *
   * @param token the token
   */
  def tokenRemoved(token : Token) : Unit
}
