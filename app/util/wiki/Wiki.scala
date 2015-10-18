package jp.gr.java_conf.hangedman.util.wiki

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import jp.gr.java_conf.hangedman.model._
import net.ceedubs.ficus._
import net.ceedubs.ficus.Ficus.{ booleanValueReader, stringValueReader, optionValueReader, toFicusConfig }
import org.joda.time.DateTime
import scala.collection.mutable.ArrayBuffer

class Wiki(setupfile: String) extends AbstractWiki {

  val config: Config = ConfigFactory.load()
  // FIXME: process when config not found
  val pluginDir = config.as[Option[String]]("setup.plugin_dir")
  val frontPage = config.as[Option[String]]("setup.frontpage")
  // FIXME: Timezone, post_max
  val storage = new DefaultStorage

  // FIXME: temporary impl
  val users: ArrayBuffer[User] = ArrayBuffer[User]()

  def GetCanShowMax(): Option[WikiPageLevel] = { Some(PublishAll) }
  def GetChildWikiDepth(): Int = { 0 }
  def addAdminHandler[T](action: Action, cls: T): Unit = {}
  def addAdminMenu(label: String, url: String, weight: Weight, desc: String): Unit = {}
  def addBlockPlugin[T](name: String, cls: T, format: WikiFormat): Unit = {}
  def addEditformPlugin(plugin: WikiPlugin, weight: Weight): Unit = {}
  def addFormatPlugin[T](name: String, cls: T): Unit = {}
  def addHandler[T](action: Action, cls: T): Unit = {}
  def addHeadInfo(info: String): Unit = {}
  def addHook[T](name: String, obj: T): Unit = {}
  def addInlinePlugin[T](name: String, cls: T, format: WikiFormat): Unit = {}
  def addMenu(name: String, href: String, weight: Weight, nofollow: Boolean): Unit = {}
  def addParagraphPlugin[T](name: String, cls: T, format: WikiFormat): Unit = {}
  def addPlugin[T](name: String, cls: T): Unit = {}
  def addUser(user: User): Unit = {
    users.append(user)
  }
  def addUser(id: String, pass: String, role: Role): Unit = {
    users.append(User(id, pass, role))
  }
  def addUserHandler[T](action: Action, cls: T): Unit = {}
  def addUserMenu(label: String, url: String, weight: Weight, desc: String): Unit = {}
  def callHandler(action: Action): String = { "" }
  def canModifyPage(pageName: String): Boolean = { true }
  def canShow(pageName: String): Boolean = { true }
  def checkLogin(id: String, pass: String, path: String): Option[LoginInfo] = { None }
  def childWikiExists(wikiName: String): Boolean = { true }
  def config(key: String): String = { "" }
  def config(key: String, value: String): Unit = {}
  def convertFromFswiki(source: String, formatType: WikiFormat, isInline: Boolean): String = { "" }
  def convertToFswiki(source: String, formatType: WikiFormat, isInline: Boolean): String = { "" }
  def createChildWiki(siteName: String, adminId: String, password: String): Unit = {}
  def createPageUrl(pageName: String): String = { "" }
  def createUrl(params: String*): String = { "" }
  def doHook(name: String, arguments: String*): Unit = {}
  def error(message: String): String = { "" }
  def farmIsEnable(): Boolean = { true }
  def freezePage(pageName: String): Unit = {}
  def getAdminMenu(): Menu = { new Menu }
  def getBackup(pageName: String, version: Int): Option[String] = { Some("") }
  def getCGI(): Unit = {}
  def getCurrentParser(): Option[Parser] = { None }
  def getEditFormat(): WikiFormat = { HTML_FORMAT }
  def getEditformPlugin(): String = { "" }
  def getFormatNames(): List[String] = { List("") }
  def getFreezeList(): List[String] = { List("") }
  def getLastModified(): org.joda.time.DateTime = { new DateTime }
  def getLastModifiedLogically(): org.joda.time.DateTime = { new DateTime }
  def getLoginInfo(): Option[LoginInfo] = { None }
  def getPage(pageName: String, format: WikiFormat): String = { "" }
  def getPageLevel(pageName: String): WikiPageLevel = { PublishAll }
  def getPageList(params: String*): List[String] = { List("") }
  def getPluginInfo(name: String): Option[PluginInfo] = { None }
  def getPluginInstance[T](cls: T): T = { cls }
  def getTitle(): String = { "" }
  def getWikiList(): List[String] = { List("") }
  def installPlugin(plugin: WikiPlugin): Unit = {}
  def isFreeze(pageName: String): Boolean = { true }
  def isInstalled(plugin: WikiPlugin): Boolean = { true }
  def pageExists(pageName: String): Boolean = { true }
  def parseInlinePlugin(text: String): (String, Array[String], String) = { ("", Array(""), "") }
  def processBeforeExit(): Unit = {}
  def processPlugin(plugin: WikiPlugin, parser: Parser): Unit = {}
  def processWiki(wikiformat: String): String = { "" }
  def redirect(pageName: String, part: Int): Unit = {}
  def redirectURL(url: java.net.URL): Unit = {}
  def removeChildWiki(path: String): Unit = {}
  def savePage(pageName: String, content: String, updateTimestamp: Boolean): Unit = {}
  def searchChild(dir: String): List[String] = { List("") }
  def setPageLevel(pageName: String, level: WikiPageLevel): Unit = {}
  def setTitle(title: String, isEditing: Boolean): Unit = {}
  def unFreezePage(pageName: String): Unit = {}
  def userIsExists(user: User): Boolean = {
    users.exists(listed => listed == user)
  }

}
