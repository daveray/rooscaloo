A simple rules engine built in Scala in the spirit of drools. Includes a fairly simple implementation of the [rete algorithm](http://en.wikipedia.org/wiki/Rete_algorithm) which, while not remotely industrial strength, is suitable for educational purposes.

_This was a personal project for learning the Scala language. It was initially implemented on Scala 2.7.2 and hosted on [google code](http://rooscaloo.googlecode.com)_

Here are some example rules. They're parsed using a Scala parser combinator that's a whopping 50 lines of code including whitespace and comments. Notice that there is Scala code embedded in the rules. This code is compiled on the fly as the rules are loaded into the rete network.

    import org.darevay.rooscaloo.demo._

    rule "Make cheddar cheese" {
      p : Person where {- p.status == "full" -}
      q : Person where {- q == p && p.name == "Zeus" -}
      not { c : Cheese where {- c.name == "cheddar" -} }
    -->
    {-
      engine.addFact(Cheese("cheddar"))
      engine.println("%s made some cheddar cheese.", p.name)
    -}
    }

    rule "Eat cheddar cheese" {
      p : Person where {- p.status == "hungry" -}
      c : Cheese where {- c.name == "cheddar"  -}
    -->
      {-
        engine.removeFact(c) // remove the cheese
        engine.println("%s is now full from eating %s", p.name, c.name)

        // Update
        engine.removeFact(p)
        engine.addFact(Person(p.name, "full"))
      -}

    <--
      {- engine.println("%s is no longer hungry, or there's no more cheese", p.name) -}
    }

    rule MonitorCheese {
      not { Cheese }
    -->
      {- engine.println("Monitor Cheese: There is no cheese") -}
    <--
      {- engine.println("Monitor Cheese: There is SOME cheese!") -}
    }

and here's some example output. A `+` indicates a fact being added to the rete network. A `-` is a fact being removed. `-->` and `<--` are rules firing and unfiring.

    0> --> [Rule: Monitor cheese, @1f9e6e5]
    0> Monitor Cheese: There is no cheese
    0> + Person(Dave,hungry)
    1> + Cheese(swiss)
    1> <-- [Rule: Monitor cheese, @1f9e6e5]
    1> Monitor Cheese: There is SOME cheese!
    2> + Person(Zeus,full)
    2> --> [Rule: Make cheddar cheese, @10bbf6d]
    2> Zeus made some cheddar cheese.
    3> + Cheese(cheddar)
    3> --> [Rule: Eat cheddar cheese, @9de832]
    3> Dave is now full from eating cheddar
    3> <-- [Rule: Make cheddar cheese, @10bbf6d]
    4> - Person(Dave,hungry)
    4> <-- [Rule: Eat cheddar cheese, @9de832]
    4> Dave is no longer hungry, or there's no more cheese
    4> - Cheese(cheddar)
    4> --> [Rule: Make cheddar cheese, @182d86]
    4> Zeus made some cheddar cheese.
    4> + Person(Dave,full)

Finally, I initially implemented an XML format using Scala's cool XML matching. Here's the same example as above, but in XML:

    <?xml version="1.0" encoding="UTF-8"?>
    <rules>
      <import name="org.darevay.rooscaloo.demo._"/>
      <rule name="Make cheddar cheese">
        <if>
          <object type="Person" binding="p"> p.status == "full" </object>
          <object type="Person" binding="q"> q == p &amp;&amp; p.name == "Zeus" </object>
          <not>
            <object type="Cheese" binding="c"> c.name == "cheddar" </object>
          </not>
        </if>
        <then>
          engine.addFact(Cheese("cheddar"))
          engine.println("%s made some cheddar cheese.", p.name)
        </then>
      </rule>
      <rule name="Eat cheddar cheese">
        <if>
          <object type="Person" binding="p"> p.status == "hungry" </object>
          <object type="Cheese" binding="c"> c.name == "cheddar" </object>
        </if>
        <then>
          engine.removeFact(c) // remove the cheese
          engine.println("%s is now full from eating %s", p.name, c.name)

          // Update
          engine.removeFact(p)
          engine.addFact(Person(p.name, "full"))
        </then>
        <unfire>
          engine.println("%s is no longer hungry, or there's no more cheese", p.name)
        </unfire>
      </rule>
      <rule name="Monitor cheese">
        <if>
          <not>
            <object type="Cheese"/>
          </not>
        </if>
        <then>
          engine.println("Monitor Cheese: There is no cheese")
        </then>
        <unfire>
          engine.println("Monitor Cheese: There is SOME cheese!")
        </unfire>
      </rule>
    </rules>
