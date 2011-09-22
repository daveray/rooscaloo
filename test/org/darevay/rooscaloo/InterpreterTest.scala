/*
 * InterpreterTest.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

class InterpreterTest {

  @Test
  def testEval() = {

    val i = new Interpreter()

    val result = i.eval(""" "hello, " + "world" """)

    assertTrue(result.isInstanceOf[String])
    assertEquals("hello, world", result toString)

    val another = i.eval("42 * 42")
    assertTrue(another.isInstanceOf[Int])
    assertEquals(42 * 42, another.asInstanceOf[Int])

    val function = i.eval("(x : Int) => x + 1")
    assertTrue(function.isInstanceOf[Function1[Int, Int]])
    val f = function.asInstanceOf[Function1[Int, Int]]
    for(n <- 1 to 100)
      assertEquals(n + 1, f(n))
  }

}
