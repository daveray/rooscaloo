/*
 * XmlParserTest.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

class XmlParserTest {

  @Before
  def setUp(): Unit = {
  }

  @After
  def tearDown(): Unit = {
  }

  @Test
  def testParseObjectCondition() {

    val c = XmlParser.parseObjectCondition(<object type="testType" binding="testBinding">This is some test code</object>)
    assertNotNull(c)
    assertEquals("testType", c.binding.typeName)
    assertEquals("testBinding", c.binding.name)
    assertEquals("This is some test code", c.testCode)
  }

  @Test
  def testParseNotCondition() {
    val c = XmlParser.parseNot(
      <not>
        <object type="testType" binding="testBinding">This is some test code</object>
        <not>
        </not>

      </not>)
    assertNotNull(c)
    assertEquals(2, c.children.size)
    val o = c.children(0).asInstanceOf[AST.ObjectConditionAST]
    assertEquals("testType", o.binding.typeName)
    assertEquals("testBinding", o.binding.name)
    assertEquals("This is some test code", o.testCode)

    val nc = c.children(1).asInstanceOf[AST.NotAST]
    assertEquals(0, nc.children.size)
  }

  @Test
  def testParseRule() {
    val rule = XmlParser.parseRule(
    <rule name="testRule">
      <if>
        <object type="testType" binding="testBinding">This is some test code</object>
      </if>
      <then>This is fire code</then>
      <unfire>This is unfire code</unfire>
    </rule>)

    assertNotNull(rule)
    assertEquals("testRule", rule.name)
    assertEquals(1, rule.conditions.size)
    assertEquals(AST.ObjectConditionAST(AST.Binding("testBinding", "testType"), "This is some test code"), rule.conditions(0))
    assertEquals("This is fire code", rule.fireCode)
    assertEquals("This is unfire code", rule.unfireCode)
  }

}
