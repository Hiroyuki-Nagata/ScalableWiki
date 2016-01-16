package jp.gr.java_conf.hangedman.model

import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import jp.gr.java_conf.hangedman.util.WikiUtil
import scala.collection.mutable.HashMap
import scala.util.{ Try, Success, Failure }

case class User(id: String, pass: String, role: Role)
case class Users(users: List[User])

sealed abstract class Role
case object Administrator extends Role
case object NormalUser extends Role

sealed abstract class WikiFormat
case object HTML_FORMAT extends WikiFormat
case object WIKI_FORMAT extends WikiFormat
case object FSWiki extends WikiFormat
case object NO_FORMAT extends WikiFormat

sealed abstract class WikiPluginType
case object Inline extends WikiPluginType
case object Paragraph extends WikiPluginType
case object Block extends WikiPluginType
case object EditForm extends WikiPluginType
case object NonSpecify extends WikiPluginType

sealed abstract class WikiPageLevel
case object PublishAll extends WikiPageLevel
case object PublishUser extends WikiPageLevel
case object PublishAdmin extends WikiPageLevel

case class LoginInfo(id: String, userType: String, path: String) extends HashMap[String, String]
case class PluginInfo(className: String, tpe: WikiPluginType, format: WikiFormat)

case class Weight(weight: Int)
case class Menu()
case class Parser(name: String)

abstract class WikiPlugin(className: String, tpe: WikiPluginType, format: WikiFormat) {

  import java.nio.file.StandardCopyOption.REPLACE_EXISTING
  import java.nio.file.Paths.get

  def install(wiki: AbstractWiki): Either[String, Boolean]

  def hook(wiki: AbstractWiki, name: String, args: Seq[String]): String

  implicit def toPath(filename: String) = get(filename)

  def copy(from: String, to: String): Either[String, Boolean] = {
    Try {
      java.nio.file.Files.copy(from, to, REPLACE_EXISTING)
    } match {
      case Success(_) =>
        Right(true)
      case Failure(e) =>
        Left(e.getStackTraceString)
    }
  }

  def glob(wildcard: String): List[String] = WikiUtil.glob(wildcard)
}

class PathInfo() {
  def length(): Int = {
    0
  }
}

class DummyCGI {
  def pathInfo(): PathInfo = {
    new PathInfo()
  }
  def removeSession(wiki: AbstractWiki) = {
  }
  def paramAction: Option[String] = {
    Some("")
  }
  def paramPage(page: String = "page"): String = {
    "dummy"
  }
  def getParam(param: String): String = {
    "dummy"
  }
  def allParameters(): Array[String] = {
    Array()
  }
}
