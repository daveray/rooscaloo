/*
 * ReteBuilderContextTest.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.rete

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

class ReteBuilderContextTest {

  @Before
  def setUp(): Unit = {
  }

  @After
  def tearDown(): Unit = {
  }

  @Test
  def testBindingLookup() = {

    val a = new ReteBuilderContext(None)
    
    a.bind('a)
    a.bind('b)
    val b = a.push()
    b.bind('b)
    b.bind('c)
    val c = b.push()
    c.bind('d)

    assertEquals(0, a('a))
    assertEquals(0, a('b))
    assertEquals(1, b('a))
    assertEquals(0, b('b))
    assertEquals(0, b('c))
    assertEquals(0, c('d))
    assertEquals(1, c('b))
    assertEquals(1, c('c))
    assertEquals(2, c('a))
  }

  @Test
  def testMissingBindingThrowsException() {
    val a = new ReteBuilderContext(None)
    try {
      a('a)
      fail("Expected IllegalArgumentException")
    } catch {
      case e : IllegalArgumentException => ()
    }
  }

  @Test
  def testAllBindings() = {
    val a = new ReteBuilderContext(None)

    a.bind('a)
    a.bind('b)
    val b = a.push()
    b.bind('b)
    b.bind('c)
    val c = b.push()
    c.bind('d)

    val all = c.allBindings

    assertEquals(2, all('a))
    assertEquals(1, all('b))
    assertEquals(1, all('c))
    assertEquals(0, all('d))

    assertEquals(4, all.size)
  }
}
