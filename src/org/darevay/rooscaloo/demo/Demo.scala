/*
 * Demo.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.demo

import java.io.InputStreamReader

import org.darevay.rooscaloo.AST
import org.darevay.rooscaloo.Engine
import org.darevay.rooscaloo.RooscalooParser
import org.darevay.rooscaloo.XmlParser

case class Person(name : String, status : String)
case class Cheese(name : String)

object Demo {


  def main(args : Array[String]) {

    val rulesXml = scala.xml.XML.load(getClass getResourceAsStream "/org/darevay/rooscaloo/demo/demo.xml")
    run("demo.xml", XmlParser.parseRuleSet(rulesXml))

    val parser = new RooscalooParser()
    val rules = parser.parseAll(parser.ruleSet, new InputStreamReader(getClass getResourceAsStream "/org/darevay/rooscaloo/demo/demo.rooscaloo"))
    run("demo.rooscaloo", rules.get)
  }
  
  def run(name : String, ruleSet : AST.RuleSetAST) {
    println(name + "-----------------------------------------------------------------")
    val engine = new Engine()

    val startLoad = System.nanoTime()
    engine.addRuleSet(ruleSet)
    println("Loaded " + ruleSet.rules.size + " rules in " + (System.nanoTime() - startLoad) / 1000000 + "ms")
    
    val startRun = System.nanoTime()
    engine.addFact(new Person("Dave", "hungry"))
    engine.elaborate()
    engine.addFact(new Cheese("swiss"))
    engine.elaborate()
    engine.addFact(new Person("Zeus", "full"))
    engine.elaborate()
    engine.elaborate()
    engine.elaborate()
    
    println("Ran " + engine.cycle + " cycles in " + (System.nanoTime() - startRun) / 1000000 + "ms")
  }
  
}
