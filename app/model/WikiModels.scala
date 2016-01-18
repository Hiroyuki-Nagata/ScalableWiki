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

sealed abstract class HandlerPermission
case object PermitAll extends HandlerPermission
case object PermitLoggedin extends HandlerPermission
case object PermitAdmin extends HandlerPermission

case class LoginInfo(id: String, userType: String, path: String) extends HashMap[String, String]
case class PluginInfo(className: String, tpe: WikiPluginType, format: WikiFormat)

case class Weight(weight: Int)
case class Menu()
case class Parser(name: String)

trait Perl {
  import java.nio.file.StandardCopyOption.REPLACE_EXISTING
  import java.nio.file.Paths.get

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

abstract class WikiPlugin(className: String, tpe: WikiPluginType, format: WikiFormat) extends Perl {
  def install(wiki: AbstractWiki): Either[String, Boolean]
  def hook(wiki: AbstractWiki, name: String, args: Seq[String]): String
}

abstract class WikiHandler(className: String, tpe: WikiPluginType, format: WikiFormat) extends Perl {
  val action = className
  def doAction(wiki: AbstractWiki): Either[String, play.api.mvc.Result]
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
