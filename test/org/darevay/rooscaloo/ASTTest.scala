/*
 * ASTTest.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import AST._
class ASTTest {

  @Before
  def setUp(): Unit = {
  }

  @After
  def tearDown(): Unit = {
  }

  @Test
  def testGenerateObjectConditionCode() {

    val ocast = AST.ObjectConditionAST(Binding("s", "java.lang.String"), "s.isEmpty()");
    assertEquals("(s_ : AnyRef, mc_ : org.darevay.rooscaloo.MatchContext) => {\n" +
                 "val s = s_.asInstanceOf[java.lang.String];\n" +
                 "val i = mc_('i).asInstanceOf[java.lang.Integer];\n" +
                 "s.isEmpty()\n" +
                 "}\n",
                 ocast.code(List(Binding("i", "java.lang.Integer"))))
  }

  @Test
  def testCompileObjectConditionCode() {
    val ocast = AST.ObjectConditionAST(Binding("s", "java.lang.String"), "s.isEmpty()");
    val oc = ocast.compile(new Interpreter(), Nil).asInstanceOf[ObjectCondition[AnyRef]]
    assertNotNull(oc)
    assertEquals('s, oc.binding.get)
    val mc = new MatchContext() {
      def apply(name : Symbol) : AnyRef = ""
    }
    assertTrue(oc.condition("", mc))
  }

}
