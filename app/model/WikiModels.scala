package jp.gr.java_conf.hangedman.model

import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki

case class User(id: String, pass: String, role: Role)
case class Users(users: List[User])

sealed abstract class Role
case object Administrator extends Role
case object NormalUser extends Role

sealed abstract class WikiFormat
case object HTML_FORMAT extends WikiFormat
case object WIKI_FORMAT extends WikiFormat
case object FSWiki extends WikiFormat

sealed abstract class WikiPluginType
case object Inline extends WikiPluginType
case object Paragraph extends WikiPluginType
case object Block extends WikiPluginType

sealed abstract class WikiPageLevel
case object PublishAll extends WikiPageLevel
case object PublishUser extends WikiPageLevel
case object PublishAdmin extends WikiPageLevel

case class LoginInfo(id: String, userType: String, path: String)
case class PluginInfo(className: String, tpe: WikiPluginType, format: WikiFormat)

case class Weight()
case class Menu()
case class Parser(name: String)

abstract class WikiPlugin(className: String, tpe: WikiPluginType, format: WikiFormat) {
  def install(wiki: AbstractWiki): Either[String, Boolean]
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
