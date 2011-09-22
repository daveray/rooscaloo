/*
 * Rete.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete

//import org.darevay.rooscaloo.Matcher
import org.darevay.rooscaloo.ObjectCondition
import org.darevay.rooscaloo.Condition
import org.darevay.rooscaloo.MatchContext
import org.darevay.rooscaloo.Matcher
import org.darevay.rooscaloo.NotCondition
import org.darevay.rooscaloo.Rule

/**
 * Implementation of a rete network
 */
class Rete extends Matcher {

  /**
   * All alpha memories indexed by object type
   */
  val alphaMemories = scala.collection.mutable.Map.empty[Class[_], AlphaMemory]

  /**
   * Map from facts to alpha memory items. Need this to be able to remove
   * facts later without liner search
   */
  val factMap = scala.collection.mutable.Map.empty[AnyRef, AlphaMemoryItem]

  /**
   * The "dummy" root beta memory that is the root of the rete network. Contains
   * a single "dummy" token so that empty rules will match automatically.
   */
  val rootBetaMemory = new BetaMemory(null) {
    newToken(None, None) // Add a dummy token
  }

  /**
   * Map from rules to rule nodes at the bottom of the network
   */
  val rules = scala.collection.mutable.Map.empty[Rule, RuleNode]

  /**
   * @see Matcher#addRule(Rule)
   */
  def addRule(rule : Rule) {
    val (bottom, finalContext) = addConditions(new ReteBuilderContext(None), null, rule.conditions)
    val beta = if(bottom == null) rootBetaMemory else bottom.buildChildMemory()
    val pnode = new RuleNode(rule, beta, finalContext.allBindings(-1))

    rules.put(rule, pnode)
    beta.updateNewChildWithMatches(pnode)
  }

  /**
   * @see Matcher#removeRule(Rule)
   */
  def removeRule(rule : Rule) {
    throw new UnsupportedOperationException("removeRule not implemented yet");
  }
  
  /**
   * @see Matcher#addFact(AnyRef)
   */
  def addFact(fact : AnyRef) : AnyRef = {
    if(!factMap.contains(fact)) {
      // Create a new item and store it in the map. Then activate the
      // appropriate alpha memory
      val item = new AlphaMemoryItem(fact)

      factMap.put(fact, item)
      getAlphaMemory(item.key).add(item)
    }
    fact
  }

  /**
   * @see Matcher#removeFact(AnyRef)
   */
  def removeFact(fact : AnyRef) : AnyRef = {
    factMap.removeKey(fact).foreach(_ remove)
    fact
  }

  private def addConditions(context: ReteBuilderContext,
                            parentNode : ConditionNode,
                            conditions : List[Condition]) : (ConditionNode, ReteBuilderContext) = {
    var tempParent = parentNode
    var tempContext = context
    for(c <- conditions) {
      val beta = if(tempParent == null) rootBetaMemory else tempParent.buildChildMemory()
      tempParent = addCondition(tempContext, beta, c)
      tempContext = tempContext.push
    }
    (tempParent, tempContext)
  }
  
  private def addCondition(context: ReteBuilderContext,
                           beta : BetaMemory, condition : Condition) : ConditionNode =  {

    condition match {
      case NotCondition(subs) => createNotNode(context, beta, subs)
      case t : ObjectCondition[_] => createObjectConditionNode(context, beta, t)
    }
  }

  private def createObjectConditionNode(context: ReteBuilderContext,
                                        parent : BetaMemory,
                                        condition : ObjectCondition[_]) : ObjectConditionNode = {
    // Add any variables bound by this condition to the current context
    context.bind(condition.binds.toList : _*)

    // Now create a new join node for the condition
    new ObjectConditionNode(parent,
                 getAlphaMemory(condition.klass),
                 (token : Token, fact : AnyRef, context : MatchContext) => condition(fact, context),
                 context.allBindings(-1))
  }

  private def createNotNode(context: ReteBuilderContext,
                            parent : BetaMemory,
                            subs : List[Condition]) : NotNode = {
    // First construct the negated subnet
    val bottomOfSubnet =
      if(subs.isEmpty)
        parent
      else
        addConditions(context, parent.parent, subs)._1.buildChildMemory()

    // Hook a not node to the parent and bottom of the subnet
    new NotNode(parent, bottomOfSubnet)
  }
  
  private[rete] def getAlphaMemory(key : Class[_]) : AlphaMemory = {
    // The second argument is "by name" so it will only be evaluated if the
    // key is not in the map. Nice.
    alphaMemories.getOrElseUpdate(key, constructAlphaMemory(key))
  }

  private def constructAlphaMemory(key : Class[_]) : AlphaMemory = {
    val sc = key.getSuperclass

    new AlphaMemory(key, if(sc != null) Some(getAlphaMemory(sc)) else None)
  }

  override def toString() = {
    rootBetaMemory.toString
  }
}
