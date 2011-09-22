/*
 * Main.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

import org.darevay.rooscaloo.RuleBuilder._
import org.darevay.rooscaloo.ObjectCondition._
import org.darevay.rooscaloo.NotCondition._

object Main {

  def main(args: Array[String]) {

    val engine = new Engine()

    class Person(val name : String, var status : String, var age : Int)
    class Cheese(val name : String)
    
    val makeCheese = Rule("Make cheddar cheese",
      // If there's a person (bound to 'p) that's full
      (classOf[Person] as 'p  where ((p, x) => { p.status == "full"})) ::
      // and that person is Zeus
      (classOf[Person] as 'q where ((q, x) => {
                                        val p = x('p).asInstanceOf[Person]
                                        q == p && p.name == "Zeus"
                                      })) ::
      // and there's no cheddar cheese
      -(classOf[Cheese] where ((c, x) => { c.name == "cheddar"})) :: Nil,
      i => {
         val p = i.context('p).asInstanceOf[Person]

         engine.addFact(new Cheese("cheddar"))
         println(p.name + " made some cheddar cheese: " + i)
      },
      Some((i : MatchInstance) => println("Retracted: " + i))
    )

    val eatCheddarCheese = Rule("Eat cheddar cheese",
      // if there's a person (bound to 'p) that's hungry
      (classOf[Person] as 'p where ((p, x) => { p.status == "hungry"})) ::
      // and there's some cheddar cheese (bound to 'c)
      (classOf[Cheese] as 'c where ((c, x) => { c.name == "cheddar"})) :: Nil,
      i => {
         // get bound variables...
         val p = i.context('p).asInstanceOf[Person]
         val c = i.context('c).asInstanceOf[Cheese]

         // remove the cheese from memory
         engine.removeFact(c)

         p.status = "full"
         p.age += 1
         // todo "update" p
         println(p.name + " is now full from eating " + c.name + ": " + i)
      },
      Some((i : MatchInstance) => println("Retracted: " + i))
    )

    engine.addRule(makeCheese)
    engine.addRule(eatCheddarCheese)
    
    engine.addFact(new Person("Dave", "hungry", 33))
    engine.elaborate()
    engine.addFact(new Cheese("swiss"))
    engine.elaborate()
    engine.addFact(new Person("Zeus", "full", 99))
    engine.elaborate()
    engine.elaborate()
  }

}
