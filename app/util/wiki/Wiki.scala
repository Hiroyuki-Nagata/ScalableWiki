package jp.gr.java_conf.hangedman.util.wiki

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import java.io.File
import java.net.URL
import java.net.URL
import java.net.URLClassLoader
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.WikiUtil
import net.ceedubs.ficus.Ficus.{ booleanValueReader, stringValueReader, optionValueReader, toFicusConfig }
import net.ceedubs.ficus._
import org.clapper.classutil.ClassFinder
import org.clapper.classutil.ClassInfo
import org.joda.time.DateTime
import play.Logger
import play.Logger
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.Results
import scala.collection.immutable.ListMap
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.matching.Regex

class Wiki(setupfile: String = "setup.conf", initRequest: Request[AnyContent])
    extends AbstractWiki with Controller {

  // load "setup.conf"
  val config: Config = ConfigFactory.parseFile(new File("conf/" + setupfile))
  val defaultConf: Config = ConfigFactory.parseFile(new File("conf/config.dat"))
  val pluginDir: String = config.as[Option[String]]("setup.plugin_dir").getOrElse(".")
  val frontPage: String = config.as[Option[String]]("setup.frontpage").getOrElse("FrontPage")
  val request = initRequest
  var configCache = HashMap[String, String]().empty

  // initialize instance variables
  val handler = HashMap.empty[String, String]
  val handlerPermission = HashMap.empty[String, String]
  val plugin = HashMap.empty[String, WikiPlugin]
  var title = ""
  val menu = ArrayBuffer.empty[String]
  //val CGI               = CGI2->new()
  val hook = HashMap.empty[String, String]
  val users = ListBuffer[User]()
  val adminMenu = ListBuffer()
  val editform = ListBuffer()
  val edit = 0
  val parseTimes = 0
  val format = HashMap.empty[String, String]
  val installedPlugin = ListBuffer[String]()
  val headInfo = ListBuffer()

  // FIXME: Timezone, post_max
  val storage = new DefaultStorage(this)
  var isEdit: Boolean = false

  def getCanShowMax(): Option[WikiPageLevel] = { Some(PublishAll) }
  def getChildWikiDepth(): Int = { 0 }
  def addAdminHandler[T](action: String, cls: T) = {
  }
  def addAdminMenu(label: String, url: String, weight: Weight, desc: String): Unit = {}
  def addBlockPlugin(name: String, cls: WikiPlugin) = {
    this.plugin += ((name, cls))
  }
  def addEditformPlugin(plugin: WikiPlugin, weight: Weight): Unit = {}
  def addFormatPlugin(name: String, cls: WikiPlugin): Unit = {}
  def addHandler[T](action: String, cls: T): Unit = {}
  def addHeadInfo(info: String): Unit = {}
  def addHook[T](name: String, obj: T): Unit = {
  }
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
  def addUserHandler[T](action: String, cls: T): Unit = {}
  def addUserMenu(label: String, url: String, weight: Weight, desc: String): Unit = {}
  def callHandler(action: String): String = { "" }
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
  /**
   * ページにジャンプするためのURLを生成するユーティリティメソッドです。
   * 引数としてページ名を渡します。
   * {{{
   * wiki.createPageUrl(&quot;FrontPage&quot;)
   * }}}
   * 上記のコードは通常、以下のURLを生成します。
   * wiki.cgi?page=FrontPage
   */
  def createPageUrl(page: String): String = {
    this.createUrl(scala.collection.immutable.HashMap("page" -> page))
  }
  /**
   * 任意のURLを生成するためのユーティリティメソッドです。
   * 引数としてパラメータのハッシュリファレンスを渡します。
   * {{{
   * wiki.createUrl(HashMap("action" -> "HOGE", "type" -> "1"))
   * }}}
   * 上記のコードは通常、以下のURLを生成します。
   * {{{
   * wiki.cgi?action=HOGE&amp;type=1
   * }}}
   */
  def createUrl(params: scala.collection.immutable.HashMap[String, String]): String = {
    val url: String = this.config("script_name").getOrElse("wiki.cgi") + "?"
    val sorted: List[String] = params.map { case (action, _) => action }.toList.sorted
    val headq: String = sorted.head match {
      case key =>
        WikiUtil.urlEncode(key) + "=" + WikiUtil.urlEncode(params(key))
    }
    val tailq: String = sorted.tail.map { key =>
      "&amp;" + WikiUtil.urlEncode(key) + "=" + WikiUtil.urlEncode(params(key))
    }.mkString

    val query = headq + tailq
    if (query.isEmpty) url else s"${url}${query}"
  }
  def createUrl(): String = { "" }
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
  def getLastModified(page: String): org.joda.time.DateTime = { new DateTime }
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
  def installPlugin(plugin: WikiPlugin): String = {
    ""
  }
  def installPlugin(pluginName: String): String = {
    Logger.debug(s"install plugin: $pluginName")

    if (pluginName.matches("""[^\p{Alnum}]""")) {
      "<div class=\"error\">" + xml.Utility.escape(s"${plugin}プラグインは不正なプラグインです。") + "</div>"
    }

    val moduleStr = s"jp.gr.java_conf.hangedman.plugin.${pluginName}.Install"
    Logger.debug(s"Start dynamic loading => $moduleStr")

    val isSuccess = Try {
      // See Also: http://software.clapper.org/classutil/
      val finder = ClassFinder()
      val classes: Iterator[ClassInfo] = finder.getClasses().iterator
      val plugins = ClassFinder.concreteSubclasses(moduleStr, classes)
      val loader = (Thread.currentThread.getContextClassLoader).asInstanceOf[URLClassLoader]

      plugins.map { moduleName =>
        Logger.info(s"Loading $moduleName...")
        val plugin = loader.loadClass(moduleName.name).newInstance.asInstanceOf[WikiPlugin]
        plugin.install(this)
      }.mkString

    } match {
      case Success(message) =>
        Logger.info(message)
        Right(message)
      case Failure(e) =>
        Logger.warn(e.getMessage)
        Left(e.getMessage)
    }

    isSuccess match {
      case Right(r) =>
        installedPlugin += pluginName
        Logger.info(s"${pluginName}プラグインをインストール完了。")
        ""
      case Left(message) =>
        Logger.warn(s"${pluginName}プラグインがインストールできません。${message}")
        "<div class=\"error\">" +
          xml.Utility.escape(s"${pluginName}プラグインがインストールできません。${message}") +
          "</div>"
    }
  }

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
  def redirect(pageName: String, part: Int = 0): Result = {
    Redirect("http://www.google.com")
  }
  def redirectURL(url: URL): Result = {
    Redirect("http://www.google.com")
  }
  def redirectURL(url: String): Result = {
    redirectURL(new URL(url))
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
