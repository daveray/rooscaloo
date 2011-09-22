/*
 * ReteBuilderContext.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete

/**
 * Context object used during rete construction. Includes offsets for looking
 * up variable bindings in conditions or on the RHS of rules. Contexts form
 * a stack. Each level corresponds to a level in the rete network along with
 * the set of variables bound up to that level.
 */
class ReteBuilderContext(val parent : Option[ReteBuilderContext]) {

  /**
   * Set of variables bound at this level of the network
   */
  private val bindings = scala.collection.mutable.Set.empty[Symbol]

  /**
   * Bind one or more variables at this level of the network
   *
   * @param syms variable argument list of variable names
   */
  def bind(syms : Symbol*) {
    bindings ++= syms
  }

  /**
   * Look up a variable binding offset. Same as apply(Symbol, Int) where
   * starting offset defaults to 0.
   *
   * @param s the variable
   * @return the offset
   * @throws IllegalArgumentException if there is no such binding
   */
  def apply(s : Symbol) : Int = apply(s, 0)

  /**
   * Look up a variable binding offset
   *
   * @param s the variable
   * @param current the starting offset
   * @return the offset
   * @throws IllegalArgumentException if there is no such binding
   */
  def apply(s : Symbol, current : Int) : Int = {
    if(bindings.contains(s))
      current
    else parent match {
      case Some(p) => p(s, current + 1)
      case None => throw new IllegalArgumentException("Unknown variable " + s)
    }
  }

  /**
   * Returns a map from all bound variables to offsets
   *
   * @return Map from all bound variables to offsets
   */
  def allBindings() : Map[Symbol, Int] = allBindings(0)

  /**
   * Returns a map from all bound variables to offsets using the given starting
   * offset.
   *
   * @param current the starting offset that is added to all calculated offsets
   * @return Map from all bound variables to offsets
   */
  def allBindings(current : Int) : Map[Symbol, Int] = {
    parent match {
      // The _* here says to take the list and treat it as varargs to the Map
      // constructor.
      case None => Map(bindings.map((_, current)).toList : _*)

      // Take bindings from parent and merge in our bindings, possibly overwriting
      case Some(p) => p.allBindings(current + 1) ++ bindings.map((_, current))
    }
  }

  /**
   * Create a new context below this one. The new context inherits all bindings
   * of this one, with offsets incremented by one.
   *
   * @return a new context that is a child of this one.
   */
  def push() : ReteBuilderContext = {
    new ReteBuilderContext(Some(this))
  }

}
