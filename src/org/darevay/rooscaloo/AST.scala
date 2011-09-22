/*
 * AST.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

object AST {

  /**
   * A variable binding in a test
   *
   * @param name the name of the variable, may be empty
   * @param typeName the name of the type
   */
  case class Binding(name : String, typeName : String) {

    /**
     * @return true if this binding is empty
     */
    def isEmpty = name.isEmpty
  }

  case class ImportAST(name : String)

  case class RuleSetAST(imports : Seq[ImportAST], rules : Seq[RuleAST])
  
  /**
   * Represents a rule in the AST
   *
   * @param name the name of the rule
   * @param fireCode code to execute on match
   * @param unfireCode code to execute on unmatch
   * @param conditions list of top-level conditions
   */
  case class RuleAST(name : String, 
                     fireCode : String,
                     unfireCode : String,
                     conditions : ConditionAST*) {

    def generate(interp : Interpreter) : Rule = {
      Rule(name,
           compile(interp, Nil, conditions.toList),
           compileAction(interp, fireCode),
           Some(compileAction(interp, unfireCode)))
    }

    def actionBindings : List[Binding] = {
      var bindings : List[Binding] = Nil
      for(c <- conditions) { bindings = c.addBindings(bindings) }
      bindings
    }

    private type ActionFunction = Function1[MatchInstance, Unit]
    
    private def compileAction(interp : Interpreter,
                              action : String) : ActionFunction = {
      val code = "(mi_ : org.darevay.rooscaloo.MatchInstance) => {\n" +
      (for(Binding(n, t) <- actionBindings)
        yield "val %s = mi_.context('%s).asInstanceOf[%s];\n" format(n, n, t)).mkString("") +
       action +
      "\n()\n}\n"

      interp.eval(code).asInstanceOf[ActionFunction]
    }
  }

  /**
   * Base class for a condition in the AST
   */
  trait ConditionAST {

    /**
     * Compiles this condition using the given interpreter and bindins and
     * returns the resulting matcher condition
     *
     * @param interp the interpreter to compile code with
     * @param bindings non-empty variable bindings in current scope
     * @return a new compiled matcher condition
     */
    def compile(interp : Interpreter, bindings : List[Binding]) : Condition

    /**
     * Add any variable bindings created by this condition to the given list
     * and return it
     *
     * @param bindings bindings to append to
     * @return new list of bindings
     */
    def addBindings(bindings : List[Binding]) : List[Binding]

  }

  /**
   * Helper function to compile a list of conditions ASTs, properly building up
   * variable bindings and handling variable scope
   *
   * @param interp the interpreter used to compile conditions
   * @param bindings current bindings
   * @param conds list of condition ASTs to compile
   * @return list of compiled matcher conditions
   */
  def compile(interp : Interpreter,
              bindings : List[Binding],
              conds : List[ConditionAST]) : List[Condition] = {
    
    // For each condition, compile it and then add its bindings to the set
    // of all bindings.
    var temp = bindings
    conds.map(c => { val r = c.compile(interp, temp);
                     temp = c.addBindings(temp);
                     r})
  }

  /**
   * Represents an object condition in the AST
   *
   * @param binding the binding created by this condition, may be empty
   * @param testCode Scala code that performs the actual test
   */
  case class ObjectConditionAST(binding : Binding,
                                testCode : String) extends ConditionAST {

    private type TestFunction = Function2[AnyRef, MatchContext, Boolean]

    def compile(interp : Interpreter, bindings : List[Binding]) : Condition = {
      // First compile the test predicate
      val function = interp.eval(code(bindings)).asInstanceOf[TestFunction]

      // Now lookup the class of the object and return a new condition object
      ObjectCondition(findClass(interp, binding.typeName),
                      Some(Symbol(binding.name)),
                      function)
    }

    private def findClass(interp : Interpreter, tn : String) : Class[AnyRef] = {
      // Do the class lookup in the interpreter. That's where the imports are.
      val code = "classOf[%s]" format tn
      interp.eval(code).asInstanceOf[Class[AnyRef]]
    }

    def addBindings(bindings : List[Binding]) : List[Binding] = {
      if (!binding.isEmpty) binding :: bindings else bindings
    }

    def code(bindings : List[Binding]) : String = {
      val bn = binding.name
      val tn = binding.typeName

      // Declare the parameter list
      "(%s_ : AnyRef, mc_ : org.darevay.rooscaloo.MatchContext) => {\n".format(bn) +
      // Cast the variable bound by this condition
      (if(!binding.isEmpty) "val %s = %s_.asInstanceOf[%s];\n".format(bn, bn, tn) else "") +
      // Lookup all the other variable bindings
      (for(Binding(n, t) <- bindings if n != binding) yield "val %s = mc_('%s).asInstanceOf[%s];\n" format(n, n, t)).mkString("") +
      // Insert the test code
      testCode +
      "\n}\n"
    }

  }

  /**
   * Represents a negated conjunctive condition in the AST.
   *
   * @param children list of child conditions in the conjunction
   */
  case class NotAST(children : ConditionAST*) extends ConditionAST {

    def compile(interp : Interpreter, bindings : List[Binding]) : Condition = {
      NotCondition(AST.compile(interp, bindings, children.toList))
    }

    def addBindings(bindings : List[Binding]) = bindings
  }

}
