/*
 * XmlParser.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo

import scala.xml
import org.darevay.rooscaloo.AST._
import org.darevay.rooscaloo.util.Util._

object XmlParser {

  def parseRuleSet(node : scala.xml.Node) : RuleSetAST = {
    RuleSetAST(for(e <- node \ "import") yield parseImport(e),
               for(e <- node \ "rule") yield parseRule(e))
  }

  def parseImport(node : scala.xml.Node) : ImportAST = {
    ImportAST((node \ "@name" text).trim)
  }

  def parseRule(node: scala.xml.Node) : RuleAST =
    RuleAST(node \ "@name" text,
            node \ "then" text,
            node \ "unfire" text,
            parseConditionList(node \ "if" \ "_") : _*)

  def parseConditionList(xml : scala.xml.NodeSeq) : Seq[ConditionAST] =
    for(e <- xml)
      yield e.label match {
        case "object" => parseObjectCondition(e)
        case "not" => parseNot(e)
      }
  
  def parseObjectCondition(xml : scala.xml.Node) : ObjectConditionAST =
    ObjectConditionAST(Binding((xml \ "@binding" text).trim,
                               (xml \ "@type" text).ifEmpty("AnyRef")),
                       xml.text.ifEmpty("true"))

  def parseNot(xml : scala.xml.Node) : NotAST =
    NotAST(parseConditionList(xml \ "_") : _*)
}
