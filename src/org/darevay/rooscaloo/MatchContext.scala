/*
 * MatchContext.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

/**
 * Interface for a "context" in the matcher. Provides access to variable
 * bindings and other information
 */
trait MatchContext {

  /**
   * Retrieves the value of a variable bound in this rule.
   *
   * @param name the name of the variable
   * @return the value of the variable
   * @throws IllegalArgumentException if the variable is not bound
   */
  def apply(name : Symbol) : AnyRef
  
}
