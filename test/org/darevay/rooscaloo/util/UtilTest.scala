/*
 * UtilTest.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.util

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

class UtilTest {

  @Before
  def setUp(): Unit = {
  }

  @After
  def tearDown(): Unit = {
  }

  @Test
  def testGetArgumentType() = {

    val f = (s : String) => s == "hello"
    assertEquals(classOf[String], Util.getArgumentType(f))
  }

  @Test
  def testToSet() = {
    import org.darevay.rooscaloo.util.Util._
    assertEquals(Set(1), Some(1) toSet)
    assertEquals(Set(), None toSet)
  }
}
