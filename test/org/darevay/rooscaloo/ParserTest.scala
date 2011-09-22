/*
 * ParserTest.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

class RooscalooParserTest {

  @Before
  def setUp(): Unit = {
  }

  @After
  def tearDown(): Unit = {
  }

  @Test
  def testRuleWithSingleAction() = {
    val p = new RooscalooParser()
    val result = p.parseAll(p.ruleSet,
       """rule Test {
       x as Object where {- test -}
       not {
          y as Integer where {- test -}
       }
       z as String
       --> {-
          action
           -}
       }
       """)
    
    println(result)
  }

  @Test
  def testRuleWithActionAndRetraction() = {
    val p = new RooscalooParser()
    val result = p.parseAll(p.ruleSet, "rule Test { --> {- action -} <-- {- action -} }")
    println(result)
  }

}
