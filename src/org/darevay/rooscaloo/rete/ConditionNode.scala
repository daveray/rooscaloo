/*
 * ConditionNode.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.darevay.rooscaloo.rete

/**
 * Base interface for condition node, i.e. nodes that perform some test and
 * store the result in a child beta memory if the test passes.
 */
trait ConditionNode {

  /**
   * The child memory
   */
  def childMemory : BetaMemory

  /**
   * Protected method called when the child memory of this node is assigned.
   * This method will only be called once.
   *
   * @param memory the child memory
   * @return the child memory
   */
  protected def childMemory_=(memory : BetaMemory) : BetaMemory

  /**
   * Constructs a new child memory for this node and assigns it, or returns
   * the current child memory if one already exists
   *
   * @return the child memory of this node
   */
  def buildChildMemory() : BetaMemory = {
    if(childMemory == null)
      childMemory = new BetaMemory(this)
    else
      childMemory
  }
}
