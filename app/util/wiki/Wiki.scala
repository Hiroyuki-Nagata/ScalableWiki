package jp.gr.java_conf.hangedman.util.wiki

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import java.io.File
import java.net.URL
import jp.gr.java_conf.hangedman.model._
import net.ceedubs.ficus._
import net.ceedubs.ficus.Ficus.{ booleanValueReader, stringValueReader, optionValueReader, toFicusConfig }
import org.joda.time.DateTime
import play.api.mvc.Controller
import play.api.mvc.Result
import play.api.mvc.Results
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex
import scala.concurrent.Future

class Wiki(setupfile: String) extends AbstractWiki with Controller {

  // load "setup.conf"
  val config: Config = ConfigFactory.parseFile(new File("conf/" + setupfile))
  val defaultConf: Config = ConfigFactory.parseFile(new File("conf/config.dat"))

  // FIXME: process when config not found
  val pluginDir = config.as[Option[String]]("setup.plugin_dir")
  val frontPage = config.as[Option[String]]("setup.frontpage")
  // FIXME: Timezone, post_max
  val storage = new DefaultStorage

  // FIXME: temporary impl
  val users: ArrayBuffer[User] = ArrayBuffer[User]()
  val installedPlugin: ArrayBuffer[WikiPlugin] = ArrayBuffer[WikiPlugin]()
  val plugin: scala.collection.mutable.Map[String, WikiPlugin] = scala.collection.mutable.Map()

  var title: String = ""
  var isEdit: Boolean = false

  def getCanShowMax(): Option[WikiPageLevel] = { Some(PublishAll) }
  def getChildWikiDepth(): Int = { 0 }
  def addAdminHandler[T](action: Action, cls: T) = {
  }
  def addAdminMenu(label: String, url: String, weight: Weight, desc: String): Unit = {}
  def addBlockPlugin(name: String, cls: WikiPlugin) = {
    this.plugin += ((name, cls))
  }
  def addEditformPlugin(plugin: WikiPlugin, weight: Weight): Unit = {}
  def addFormatPlugin(name: String, cls: WikiPlugin): Unit = {}
  def addHandler[T](action: Action, cls: T): Unit = {}
  def addHeadInfo(info: String): Unit = {}
  def addHook[T](name: String, obj: T): Unit = {}
  def addInlinePlugin(name: String, cls: WikiPlugin) = {
    this.plugin += ((name, cls))
  }
  def addMenu(name: String, href: String, weight: Weight, nofollow: Boolean): Unit = {}
  def addParagraphPlugin(name: String, cls: WikiPlugin) = {
    this.plugin += ((name, cls))
  }
  def addPlugin(name: String, cls: WikiPlugin) = {
    addInlinePlugin(name, cls)
  }
  def addPlugin(name: String, cls: String) = {

  }
  def addUser(user: User): Unit = {
    users.append(user)
  }
  def addUser(id: String, pass: String, role: Int) = {
    role match {
      case 0 =>
        users.append(User(id, pass, Administrator))
      case _ =>
        users.append(User(id, pass, NormalUser))
    }
  }
  def addUser(id: String, pass: String, role: Role) = {
    users.append(User(id, pass, role))
  }
  def addUserHandler[T](action: Action, cls: T): Unit = {}
  def addUserMenu(label: String, url: String, weight: Weight, desc: String): Unit = {}
  def callHandler(action: Action): String = { "" }
  def canModifyPage(pageName: String): Boolean = { true }
  def canShow(pageName: String): Boolean = { true }
  def checkLogin(id: String, pass: String, path: String): Option[LoginInfo] = { None }
  def childWikiExists(wikiName: String): Boolean = { true }
  def config(key: String): Option[String] = {
    (config.as[Option[String]](key), defaultConf.as[Option[String]](key)) match {
      case (Some(l), None) =>
        Some(l)
      case (None, Some(r)) =>
        Some(r)
      case (_, _) =>
        None
    }
  }
  def config(key: String, value: String): Unit = {
    config.withValue(key, ConfigValueFactory.fromAnyRef(value))
  }
  def convertFromFswiki(source: String, formatType: WikiFormat, isInline: Boolean = false): String = { "" }
  def convertToFswiki(source: String, formatType: WikiFormat, isInline: Boolean = false): String = { "" }
  def createChildWiki(siteName: String, adminId: String, password: String): Unit = {}
  def createPageUrl(pageName: String): String = { "" }
  def createUrl(params: String*): String = { "" }
  def doHook(name: String, arguments: String*): Unit = {}
  def error(message: String): String = {
    setTitle("エラー")
    "<div class=\"error\">%s</div>".format(xml.Utility.escape(message))
  }
  def farmIsEnable(): Boolean = { true }
  def freezePage(pageName: String): Unit = {}
  def getAdminMenu(): Menu = { new Menu }
  def getBackup(pageName: String, version: Int): Option[String] = {
    this.storage.getBackup(pageName, version)
  }
  def getCGI(): DummyCGI = {
    new DummyCGI
  }
  def getCurrentParser(): Option[Parser] = { None }
  def getEditFormat(): WikiFormat = { HTML_FORMAT }
  def getEditformPlugin(): String = { "" }
  def getFormatNames(): List[String] = { List("") }
  def getFreezeList(): List[String] = { List("") }
  def getLastModified(): org.joda.time.DateTime = { new DateTime }
  def getLastModifiedLogically(): org.joda.time.DateTime = { new DateTime }
  def getLoginInfo(): Option[LoginInfo] = { None }
  def getPage(pageName: String, format: WikiFormat): String = {

    val pattern = new Regex("""(^.*?[^:]):([^:].*?$)""", "path", "page")

    (pageName, format) match {
      case (pattern(path, page), FSWiki) =>
        this.storage.getPage(page, path)
      case (pattern(path, page), _) =>
        convertFromFswiki(this.storage.getPage(page, path), format)
      case _ =>
        "" // FIXME: Error
    }
  }
  def getPageLevel(pageName: String): WikiPageLevel = { PublishAll }
  def getPageList(params: String*): List[String] = { List("") }
  def getPluginInfo(name: String): Option[PluginInfo] = { None }
  def getPluginInstance[T](cls: T): T = { cls }
  def getTitle(): String = {
    this.title
  }
  def getWikiList(): List[String] = { List("") }
  def installPlugin(plugin: WikiPlugin): Unit = {}
  def installPlugin(pluginName: String): Unit = {}
  def isFreeze(pageName: String): Boolean = {

    val pattern = new Regex("""(^.*?[^:]):([^:].*?$)""", "path", "page")

    pageName match {
      case pattern(path, page) =>
        this.storage.isFreeze(page, path)
      case _ =>
        false
    }
  }
  def isInstalled(pluginName: String): Boolean = true
  def isInstalled(plugin: WikiPlugin): Boolean = {
    installedPlugin.exists(installed => installed == plugin)
  }
  def pageExists(pageName: String): Boolean = {

    val pattern = new Regex("""(^.*?[^:]):([^:].*?$)""", "path", "page")

    pageName match {
      case pattern(path, page) if page.contains(".") =>
        false // InterWiki format can't contain dot.
      case pattern(path, page) =>
        this.storage.pageExists(page, path)
      case _ =>
        false
    }
  }
  def parseInlinePlugin(text: String): (String, Array[String], String) = { ("", Array(""), "") }
  def processBeforeExit(): Unit = {}
  def processPlugin(plugin: WikiPlugin, parser: Parser) = {
  }
  def processWiki(wikiformat: String): String = { "" }
  def redirect(pageName: String, part: Int = 0): Future[Result] = {
    Future.successful(Redirect("http://www.google.com"))
  }
  def redirectURL(url: URL): Future[Result] = {
    Future.successful(Redirect("http://www.google.com"))
  }
  def removeChildWiki(path: String): Unit = {}
  def savePage(pageName: String, content: String, updateTimestamp: Boolean): Unit = {}
  def searchChild(dir: String): List[String] = { List("") }
  def setPageLevel(pageName: String, level: WikiPageLevel) = {
    storage.setPageLevel(pageName, level)
  }
  def setTitle(title: String, isEditing: Boolean = false) = {
    this.title = title
    this.isEdit = isEditing
  }
  def unFreezePage(pageName: String): Unit = {}

  def userIsExists(id: String): Boolean = {
    users.exists(listed => listed.id == id)
  }
  def userIsExists(user: User): Boolean = {
    users.exists(listed => listed == user)
  }
}
