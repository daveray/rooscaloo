/*
 * ReteTest.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete
import java.lang.Integer
import java.lang.Object
import java.lang.Integer

import org.darevay.rooscaloo.MatchContext
import org.darevay.rooscaloo.NotCondition
import org.darevay.rooscaloo.ObjectCondition
import org.darevay.rooscaloo.Rule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.After
import org.junit.Before
import org.junit.Test

class ReteTest {

  @Before
  def setUp(): Unit = {
  }

  @After
  def tearDown(): Unit = {
  }

  @Test
  def testGetAlphaMemoryForHierarchy() =  {
    val rete = new Rete()

    val arrayList = rete.getAlphaMemory(classOf[java.util.ArrayList[_]])

    assertNotNull(arrayList)
    assertEquals(classOf[java.util.ArrayList[_]], arrayList.key)
    assertEquals(classOf[java.util.AbstractList[_]], arrayList.parent.get.key)
    assertEquals(classOf[java.util.AbstractCollection[_]], arrayList.parent.get.parent.get.key)
    assertEquals(classOf[java.lang.Object], arrayList.parent.get.parent.get.parent.get.key)
    assertEquals(None, arrayList.parent.get.parent.get.parent.get.parent)
  }
  
  @Test
  def testGetAlphaMemoryDoesNotCreateDuplicates() = {
    val rete = new Rete()

    // ArrayList and Vector share a parent class (AbstractCollection) so
    // their alpha memories should have the same parent
    val arrayList = rete.getAlphaMemory(classOf[java.util.ArrayList[_]])
    val vector = rete.getAlphaMemory(classOf[java.util.Vector[_]])

    assertNotNull(arrayList)
    assertNotNull(vector)
    assertSame(arrayList.parent.get, vector.parent.get)
  }

  @Test
  def testAddRuleWithNoConditions() {
    val rete = new Rete()

    val rule = Rule("testAddRuleWithNoNegations", List(), (MatchContext) => (), None)
    rete.addRule(rule)
    assertEquals(1, rete.rootBetaMemory.children.size)
    val pnode = rete.rootBetaMemory.children.first.asInstanceOf[RuleNode]
    assertNotNull(pnode)
    assertSame(rete.rootBetaMemory, pnode.parent)
  }

  @Test def testAddRuleWithOneObjectCondition() {
    val rete = new Rete()

    val op = (s : String, c : MatchContext) => true
    val rule = Rule("testAddRuleWithOneTypeCondition",
                    List(ObjectCondition(classOf[String], op)),
                    (MatchContext) => (), None)
    rete.addRule(rule)
    assertEquals(1, rete.rootBetaMemory.children.size)
    val join = rete.rootBetaMemory.children.first.asInstanceOf[ObjectConditionNode]
    assertSame(rete.rootBetaMemory, join.parent)

    val am = rete.getAlphaMemory(classOf[String])
    assertSame(am, join.alphaMemory)

    val subJoin = join.childMemory
    assertEquals(1, subJoin.children.size)

    val pnode = subJoin.children.first.asInstanceOf[RuleNode]
    assertSame(subJoin, pnode.parent)
  }

  @Test def testSimpleMatch() {
    val rete = new Rete()

    var matches = 0
    var unmatches = 0
    val rule = Rule("testSimpleMatch",
      List(ObjectCondition(classOf[String], (s : String, c : MatchContext) => {s == "hello"}),
           ObjectCondition(classOf[java.lang.Integer], (i : java.lang.Integer, c : MatchContext) => { i.intValue() > 3})),
           (MatchInstance) => matches += 1,
           Some((MatchInstance) => unmatches += 1))
  
    rete.addRule(rule)
    assertEquals(0, matches)
    assertEquals(0, unmatches)

    rete.addFact("nomatch")
    assertEquals(0, matches)
    assertEquals(0, unmatches)

    rete.addFact(Integer.valueOf(4))
    assertEquals(0, matches)
    assertEquals(0, unmatches)

    rete.addFact("hello")
    assertEquals(1, matches)
    assertEquals(0, unmatches)

    rete.removeFact("nomatch")
    assertEquals(1, matches)
    assertEquals(0, unmatches)

    rete.removeFact(Integer.valueOf(4))
    assertEquals(1, matches)
    assertEquals(1, unmatches)
  }

  @Test def testEmpytNotCondition() {
    val rete = new Rete()

    var matches = 0
    var unmatches = 0
    val rule = Rule("testEmpytNotCondition",
                    List(NotCondition(Nil)),
                    (MatchContext) => matches += 1,
                    Some((MatchContext) => unmatches += 1))
    rete.addRule(rule)

    assertEquals(1, matches)
  }
  
  @Test def testNotConditionMatch() {
    val rete = new Rete()

    var matches = 0
    var unmatches = 0
    val rule = Rule("testNotConditionMatch",
      List( ObjectCondition(classOf[String], (s : String, c : MatchContext) => {s == "hello"}),
           -(ObjectCondition(classOf[java.lang.Integer], (i : java.lang.Integer, c : MatchContext) => { i.intValue() > 3}))
           ),
           (MatchInstance) => matches += 1,
           Some((MatchInstance) => unmatches += 1))

    rete.addRule(rule)
    assertEquals(0, matches)
    assertEquals(0, unmatches)

    rete.addFact("hello")
    assertEquals(1, matches)
    assertEquals(0, unmatches)

    rete.addFact(Integer.valueOf(4))
    assertEquals(1, matches)
    assertEquals(1, unmatches)

    rete.removeFact(Integer.valueOf(4))
    assertEquals(2, matches)
    assertEquals(1, unmatches)

    rete.removeFact("hello")
    assertEquals(2, matches)
    assertEquals(2, unmatches)
  }

  @Test def testSimpleBindings() {
    val rete = new Rete()

    var matches = 0
    var unmatches = 0
    val rule = Rule("testSimpleBindings",
      List(ObjectCondition(classOf[String], Some('s), (s : String, c : MatchContext) => { s == "hello" }),
           ObjectCondition(classOf[java.lang.Integer],
                           (i : java.lang.Integer, c : MatchContext) => 
                           {val b = c('s)
                            i.intValue() > b.asInstanceOf[String].length})),
      (MatchInstance) => matches += 1,
      Some((MatchInstance) => unmatches += 1))
  
    rete.addRule(rule)

    rete.addFact("goodbye")
    assertEquals(0, matches)
    assertEquals(0, unmatches)

    rete.addFact(Integer.valueOf(5))
    assertEquals(0, matches)
    assertEquals(0, unmatches)

    rete.addFact("hello")
    assertEquals(0, matches)
    assertEquals(0, unmatches)

    rete.addFact(Integer.valueOf(6))
    assertEquals(1, matches)
    assertEquals(0, unmatches)

    rete.removeFact("goodbye")
    assertEquals(1, matches)
    assertEquals(0, unmatches)
  }
}
