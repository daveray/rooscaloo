/*
 * Engine.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

import org.darevay.rooscaloo.rete.Rete

class Engine extends Matcher {

  private val matcher = new Rete()
  
  /**
   * Map from external rules to internal rules. Internal rules have extended
   * fire and unfire handlers where we can do additional bookkeeping.
   */
  private val ruleMap = scala.collection.mutable.Map.empty[Rule, Rule]

  /**
   * Facts buffered for addition during next elaboration
   */
  private var toAdd = List[AnyRef]()

  /**
   * Facts buffered for removal during next elaboration
   */
  private var toRemove = List[AnyRef]()

  private val interpreter = new Interpreter()
  interpreter.bind("engine", this)

  private var cycleImpl = 0

  /**
   * Compile and add the rule set described by the given AST.
   *
   * @param the rule AST to compile and add
   */
  def addRuleSet(ast : AST.RuleSetAST) {
    require(ast != null)

    ast.imports.foreach(i => interpreter.exec("import " + i.name))
    ast.rules.foreach(r => addRule(r generate interpreter))
  }

  /**
   * Add a rule to the engine
   *
   * @param rule the rule to add
   */
  def addRule(rule : Rule) {
    require(rule != null)

    val internalFire = (instance : MatchInstance) => {
      println("--> %s", instance)
      rule.fire(instance)
    }
    val internalUnfire = rule.unfire match {
      case Some(unfire) => (instance : MatchInstance) => {
          println("<-- %s", instance)
          unfire(instance)
      }
      case None => (instance : MatchInstance) => {
          println("<-- %s", instance)
      }
    }
    val internalRule = Rule(rule.name, rule.conditions,
                        internalFire,
                        Some(internalUnfire))

    matcher.addRule(internalRule)

    ruleMap += rule -> internalRule
  }

  /**
   * Remove a rule from the engine
   *
   * @param rule the rule to remove
   */
  def removeRule(rule : Rule) {
    require(rule != null)

    throw new UnsupportedOperationException("removeRule is not implemented yet")
  }

  /**
   * Add a fact to the engine. No rules will fire or retract until
   * elaborate() is called.
   *
   * @param fact the fact to add to the engine
   * @return the fact
   */
  def addFact(fact : AnyRef) : AnyRef = {
    require(fact != null)

    toAdd = fact :: toAdd
    fact
  }

  /**
   * Remove a fact from the engine. No rules will fire or retract until
   * elaborate() is called
   *
   * @param fact the fact to remove from the engine
   * @return the fact
   */
  def removeFact(fact : AnyRef) : AnyRef = {
    require(fact != null)

    toRemove = fact :: toRemove
    fact
  }

  /**
   * Run one elaboration cycle of the engine. Buffered fact additions and
   * removals are applied and any affected rules will fire or retract
   * appropriately.
   *
   * @return true if more working memory modifications are pending after
   *    performing a single elaboration.
   */
  def elaborate() : Boolean = {

    // Work off temporary references to the lists so we know we're only applying
    // the changes that are pending at the start of this procedure. Otherwise,
    // rules fired (or retracted) by added facts may modify toRemove.
    val tempToAdd = toAdd
    val tempToRemove = toRemove

    toAdd = Nil
    toRemove = Nil

    tempToRemove.foreach(internalRemoveFact)
    tempToAdd.foreach(internalAddFact)

    cycleImpl += 1
    
    !toAdd.isEmpty || !toRemove.isEmpty
  }

  private def internalAddFact(fact : AnyRef) {
    println("+ %s", fact)
    matcher addFact fact
  }

  private def internalRemoveFact(fact : AnyRef) {
    println("- %s", fact)
    matcher removeFact fact
  }
  
  /**
   * @return the current elaboration cycle of the engine
   */
  def cycle : Int = cycleImpl

  def println(s : String, args : Any*) {
    Predef.println(cycle + "> " + s.format(args : _*))
  }
}
