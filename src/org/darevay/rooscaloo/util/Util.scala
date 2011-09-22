/*
 * Util.scala
 *
 * (c) 2009, Dave Ray <daveray@gmail.com>
 */

package org.darevay.rooscaloo.util

object Util {

  def getArgumentType(f : Function1[_, _]) : Class[_] = {
    val fc = f.getClass
    val ams = fc.getDeclaredMethods.filter(_.getName == "apply")

    val am = ams.find(_.getParameterTypes()(0) != classOf[java.lang.Object])
    am.get.getParameterTypes()(0)
  }

  implicit def optionToToSet[T](o : Option[T]) = {
    case class Impl {
      def toSet : Set[T] = {
        o match {
          case Some(some) => Set(some)
          case None => Set()
        }
      }
    }
    new Impl
  }

  /**
   * Defines implicit method ifEmpty which returns its string parameter if the
   * trimmed target string is <code>null</code> or empty.
   */
  implicit def stringToIfEmpty(s : String) = {
    case class IfEmpty {
      def ifEmpty(other : String) : String = {
        val t = if(s != null) s.trim else ""
        if (t.isEmpty) other else t
      }
    }
    new IfEmpty
  }
}
