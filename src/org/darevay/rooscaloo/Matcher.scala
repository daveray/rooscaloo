/*
 * Matcher.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

/**
 * Base interface for a generic rule matcher
 */
trait Matcher {

  def addRule(rule : Rule) : Unit
  def removeRule(rule : Rule) : Unit
  def addFact(fact : AnyRef) : AnyRef
  def removeFact(fact : AnyRef) : AnyRef
}
