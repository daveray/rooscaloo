/*
 * Parser.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

import scala.util.parsing.combinator._

import org.darevay.rooscaloo.AST._
import org.darevay.rooscaloo.util.Util._

class RooscalooParser extends JavaTokenParsers {

  private val CodePattern = """\{-(?s)(.*?)-\}""".r

  def ruleSet : Parser[RuleSetAST] = imports ~ rep(rule) ^^ {
    case imps ~ rules => RuleSetAST(imps, rules)
  }

  def imports : Parser[List[ImportAST]] = rep(importStatement)

  def importStatement : Parser[ImportAST] = "import" ~> repsep(ident, ".") ^^ { 
    parts => ImportAST(parts mkString ".")
  }

  def rule : Parser[RuleAST] = "rule" ~> (ident | stringLiteral) ~ "{" ~ conditions ~ fire ~ unfire ~ "}" ^^
  {
    case n ~ "{" ~ c ~ f ~ Some(u) ~ "}" => RuleAST(n, f, u, c : _*)
    case n ~ "{" ~ c ~ f ~ None ~ "}" => RuleAST(n, f, "", c : _*)
  }

  def conditions : Parser[List[ConditionAST]] = rep(condition)

  def condition : Parser[ConditionAST] = not | obj

  def not : Parser[NotAST] = "not" ~> "{" ~> conditions <~ "}" ^^
                 (NotAST(_ : _*))

  def obj : Parser[ObjectConditionAST] =
    opt(ident <~ ":") ~ident ~  opt("where" ~> test) ^^
    {
      case Some(n) ~ t ~ Some(c) => ObjectConditionAST(Binding(n, t), c ifEmpty "true")
      case Some(n) ~ t ~ None    => ObjectConditionAST(Binding(n, t), "true")
      case None    ~ t ~ Some(c) => ObjectConditionAST(Binding("", t), c ifEmpty "true")
      case None    ~ t ~ None    => ObjectConditionAST(Binding("", t), "true")
    }

  def test : Parser[String] = CodePattern ^^ (s => { val CodePattern(code) = s; code })

  def fire : Parser[String] = "-->" ~> code

  def unfire : Parser[Option[String]] = opt("<--" ~> code)

  def code : Parser[String] = CodePattern ^^ (s => { val CodePattern(code) = s; code })
}
