package jp.gr.java_conf.hangedman.model

case class User(id: String, pass: String, role: Role)

sealed abstract class Role
case object Administrator extends Role
case object NormalUser extends Role

sealed abstract class WikiFormat
case object HTML_FORMAT extends WikiFormat
case object WIKI_FORMAT extends WikiFormat

sealed abstract class WikiPluginType
case object Inline extends WikiPluginType
case object Paragraph extends WikiPluginType
case object Block extends WikiPluginType

sealed abstract class WikiPageLevel
case object PublishAll extends WikiPageLevel
case object PublishUser extends WikiPageLevel
case object PublishAdmin extends WikiPageLevel

case class LoginInfo(id: String, userType: String, path: String)
case class PluginInfo(className: String, tpe: WikiPlugin, format: WikiFormat)

case class Weight()
case class Menu()
case class Action()

abstract class WikiPlugin {
}

abstract class Parser {
}
