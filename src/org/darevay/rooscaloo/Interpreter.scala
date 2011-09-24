/*
 * Interpreter.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

import java.io.{ StringWriter, PrintWriter }

import javax.script.ScriptException;

import scala.tools.nsc.InterpreterResults._
//import scala.tools.nsc.{Interpreter => ScalaInterpreter, Settings}
import scala.tools.nsc._
import scala.tools.nsc.interpreter._

class ResultHolder(var value : Any)

class Interpreter {
  // http://lampsvn.epfl.ch/trac/scala/ticket/874
  // http://lampsvn.epfl.ch/svn-repos/scala/scala/trunk/src/compiler/scala/tools/nsc/Interpreter.scala

  private val writer = new java.io.StringWriter()
  //  private val interpreter = new ScalaInterpreter(new Settings(), new PrintWriter(writer));

  // cf http://stackoverflow.com/questions/4121567/embedded-scala-repl-inherits-parent-classpath
  lazy val urls = java.lang.Thread.currentThread.getContextClassLoader match {
    case cl: java.net.URLClassLoader => cl.getURLs.toList
    case _ => error("classloader is not a URLClassLoader")
  }
  
  lazy val classpath = urls map { _.toString }
  val settings = new Settings()
  
  // The above code gets you the classpath in current context.
  settings.classpath.value = classpath.distinct.mkString(java.io.File.pathSeparator)

  private val interpreter = new IMain(settings, new PrintWriter(writer));

  /**
   * Bind the given value to the given variable name in the interpreter
   *
   * @param name the variable name
   * @param value the value
   */
  def bind(name : String, value : AnyRef) {
    interpreter.bind(name, value.getClass.getName, value)
  }

  /**
   * Execute a string of Scala code and ignore its result.
   *
   * @param code the code to execute 
   * @throws ScriptException if the code fails to parse
   */
  def exec(code : String) {
    // Clear the previous output buffer
    writer.getBuffer.setLength(0)

    // Execute the code and catch the result
    val ir = interpreter.interpret(code);

    // Return value or throw an exception based on result
    ir match {
      case Success => ()
      case Error => throw new ScriptException("error in: '" + code + "'\n" + writer toString)
      case Incomplete => throw new ScriptException("incomplete in :'" + code + "'\n" + writer toString)
    }
  }

  /**
   * Evaluate the given string of Scala code and return the result. The code
   * must evaluate to a value.
   *
   * @param code the code to evaluate
   * @return the result
   * @throws ScriptException if the code fails to parse
   */
  def eval(code : String) : Any = {
    //println("-------\n" + code)
    // Clear the previous output buffer
    writer.getBuffer.setLength(0)

    // Create an object to hold the result and bind in the interpreter
    val holder = new ResultHolder(null)
    bind("$result__", holder);

    // Execute the code and catch the result
    val ir = interpreter.interpret("$result__.value = " + code);

    // Return value or throw an exception based on result
    ir match {
      case Success => holder.value
      case Error => throw new ScriptException("error in: '" + code + "'\n" + writer toString)
      case Incomplete => throw new ScriptException("incomplete in :'" + code + "'\n" + writer toString)
    }
  }
}
