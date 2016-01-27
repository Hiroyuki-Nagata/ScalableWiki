package jp.gr.java_conf.hangedman.util.wiki

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import java.io.File
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.WikiUtil
import net.ceedubs.ficus.Ficus.{ booleanValueReader, stringValueReader, optionValueReader, toFicusConfig }
import net.ceedubs.ficus._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.iteratee.Enumerator
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.ResponseHeader
import play.api.mvc.Result
import play.api.mvc.Result
import play.api.mvc.Results
import scala.collection.immutable.ListMap
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.tools.nsc.doc.Universe
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.matching.Regex

class Wiki(setupfile: String = "setup.conf", initRequest: Request[AnyContent])
    extends AbstractWiki with Controller {

  // load "setup.conf"
  var config: Config = ConfigFactory.parseFile(new File("conf/" + setupfile))
  val defaultConf: Config = ConfigFactory.parseFile(new File("conf/config.dat"))
  val pluginDir: String = config.as[Option[String]]("setup.plugin_dir").getOrElse(".")
  val frontPage: String = config.as[Option[String]]("setup.frontpage").getOrElse("FrontPage")

  // set request
  val request = initRequest
  // get session
  def getSession(): play.api.mvc.Session = {
    request.session
  }
  // ruby like method
  def params(key: String): String = {
    request.queryString.get(key) match {
      case Some(value) if (value.size == 1) =>
        value.head
      case Some(value) if (value.size != 1) =>
        value.mkString(",")
      case None =>
        ""
    }
  }

  var configCache = HashMap[String, String]().empty
  var hooks = HashMap[String, WikiPlugin]().empty
  var handlers = HashMap[String, WikiHandler]().empty
  var handlerPermissons = HashMap[String, HandlerPermission]().empty

  // initialize instance variables
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
  val returnMenu: String = "<div class=\"comment\"><a href=\"" +
    this.createUrl(scala.collection.immutable.HashMap("action" -> "LOGIN")) +
    "\">メニューに戻る</a></div>"

  def getCanShowMax(): Option[WikiPageLevel] = { Some(PublishAll) }
  def getChildWikiDepth(): Int = { 0 }
  /**
   * 管理者用のアクションハンドラを追加します。
   * このメソッドによって追加されたアクションハンドラは管理者としてログインしている場合のみ実行可能です。
   * それ以外の場合はエラーメッセージを表示します。
   * {{{
   * wiki.addAdminHandler(actionパラメータ,アクションハンドラのクラス名)
   * }}}
   */
  def addAdminHandler(action: String, obj: WikiHandler) = {
    handlers.put(action, obj)
    handlerPermissons.put(action, PermitAdmin)
  }
  def addAdminMenu(label: String, url: String, weight: Weight, desc: String): Unit = {}
  def addBlockPlugin(name: String, cls: WikiPlugin) = {
    this.plugin += ((name, cls))
  }
  def addEditformPlugin(plugin: WikiPlugin, weight: Weight): Unit = {}
  def addFormatPlugin(name: String, cls: WikiPlugin): Unit = {}
  /**
   * アクションハンドラプラグインを追加します。
   * リクエスト時にactionというパラメータが一致するアクションが呼び出されます。
   * {{{
   * wiki.addHandler(actionパラメータ,アクションハンドラのクラス名)
   * }}}
   */
  def addHandler(action: String, obj: WikiHandler): Unit = {
    handlers.put(action, obj)
    handlerPermissons.put(action, PermitAll)
  }
  def addHeadInfo(info: String): Unit = {}
  def addHook(name: String, obj: WikiPlugin): Unit = {
    Logger.debug(s"Adding hook ${name} for ${obj}")
    hooks.put(name, obj)
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
  /**
   * ログインユーザ用のアクションハンドラを追加します。
   * このメソッドによって追加されたアクションハンドラはログインしている場合のみ実行可能です。
   * それ以外の場合はエラーメッセージを表示します。
   * {{{
   * wiki.addUserHandler(actionパラメータ,アクションハンドラのクラス名)
   * }}}
   */
  def addUserHandler(action: String, obj: WikiHandler): Unit = {
    handlers.put(action, obj)
    handlerPermissons.put(action, PermitLoggedin)
  }
  def addUserMenu(label: String, url: String, weight: Weight, desc: String): Unit = {}

  private def doActionWrapper(handler: WikiHandler): Either[String, play.api.mvc.Result] = {
    handler.doAction(this) match {
      case Left(message) =>
        Left(message)
      case Right(_) =>
        Right(Result(
          header = ResponseHeader(200, Map(CONTENT_TYPE -> "text/plain")),
          body = Enumerator(returnMenu.getBytes())
        ))
    }
  }
  /**
   * add_handlerメソッドで登録されたアクションハンドラを実行します。
   * アクションハンドラのdo_actionメソッドの戻り値を返します。
   * {{{
   * val content = wiki.callHandler(actionパラメータ)
   * }}}
   */
  def callHandler(action: String): Either[String, play.api.mvc.Result] = {
    if (!handlers.isDefinedAt(action) || !handlerPermissons.isDefinedAt(action)) {
      errorL("不正なアクションです。")
    } else {
      val handler: WikiHandler = handlers(action)
      Logger.debug(s"Call a handler of ${handler.action}")
      handlerPermissons(action) match {
        case PermitAdmin =>
          // Action for a admin
          getLoginInfo match {
            case None =>
              errorL("ログインしていません。")
            case Some(loginInfo) if (loginInfo.tpe == Administrator) =>
              errorL("管理者権限が必要です。")
            case Some(loginInfo) =>
              doActionWrapper(handler)
          }
        case PermitLoggedin =>
          // Action for logged-in users
          getLoginInfo match {
            case None =>
              errorL("ログインしていません。")
            case Some(loginInfo) =>
              doActionWrapper(handler)
          }
        // Normal action
        case PermitAll =>
          doActionWrapper(handler)
      }
    }
  }
  def canModifyPage(pageName: String): Boolean = { true }
  def canShow(pageName: String): Boolean = { true }
  def checkLogin(id: String, pass: String): Option[LoginInfo] = { None }
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
    Logger.debug(s"Overwrite config key: $key, value: $value")
    config = config.withValue(key, ConfigValueFactory.fromAnyRef(value))
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
  /**
   * addHookメソッドで登録されたフックプラグインを実行します。
   * 引数にはフックの名前に加えて任意のパラメータを渡すことができます。
   * これらのパラメータは呼び出されるクラスのhookメソッドの引数として渡されます。
   *
   * {{{
   * wiki.doHook(フック名[,引数1[,引数2...]])
   * }}}
   */
  def doHook(name: String, arguments: String*): Unit = {
    Logger.debug(s"Do hook ${name}")
    hooks.foreach {
      case (name, obj) =>
        Logger.debug(s"Adding hook $name, $arguments")
        obj.hook(this, name, arguments)
    }
  }
  def error(message: String): String = {
    setTitle("エラー")
    "<div class=\"error\">%s</div>".format(xml.Utility.escape(message))
  }
  def errorL(message: String): Either[String, play.api.mvc.Result] = {
    Left(this.error(message))
  }
  def farmIsEnable(): Boolean = { true }
  def freezePage(pageName: String): Unit = {}
  def getAdminMenu(): List[Menu] = {
    List(new Menu("", "", ""))
  }
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
    Logger.info(s"install plugin: $pluginName")

    if (!pluginName.matches("""^[a-zA-Z0-9]*$""")) {
      val message = s"${pluginName}プラグインは不正なプラグインです。"
      Logger.error(message)
      error(message)
    } else if (installedPlugin.exists(installed => installed == pluginName)) {
      val message = s"${pluginName}プラグインはすでにインストール済みです。"
      Logger.debug(message)
      ""
    } else {
      import scala.reflect.runtime.universe
      import jp.gr.java_conf.hangedman.plugin.InstallTrait

      val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
      val moduleStr = s"jp.gr.java_conf.hangedman.plugin.${pluginName}.Install"
      Logger.debug(s"Start dynamic loading => $moduleStr")

      Try(runtimeMirror.staticModule(moduleStr)) match {
        case Failure(e) =>
          val message = "プラグインは存在しません。"
          Logger.error(message, e)
          error(message)

        case Success(module) =>
          val message: Either[String, String] = Try {
            val obj = runtimeMirror.reflectModule(module)
            val install = obj.instance.asInstanceOf[InstallTrait]
            install.install(this)
          } match {
            case Success(_) =>
              Right("プラグインのインストールに成功しました。")
            case Failure(e) =>
              Left("プラグインのインストールに失敗しました。")
          }

          message match {
            case Left(m) =>
              Logger.error(m)
              error(m)
            case Right(m) =>
              Logger.info(m)
              ""
          }
      }
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
