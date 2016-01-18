package jp.gr.java_conf.hangedman.util.wiki

import java.net.URL
import jp.gr.java_conf.hangedman.model._
import org.joda.time.DateTime
import play.api.mvc.AnyContent
import play.api.mvc.Request
import play.api.mvc.Result
import scala.collection.mutable.HashMap
import scala.concurrent.Future

abstract class AbstractWiki {

  // request information
  val request: Request[AnyContent]
  var configCache: HashMap[String, String]

  /**
   * Add user; specify 0 if user is admin, else 1
   */
  def addUser(id: String, pass: String, role: Role)
  def addUser(user: User)
  /**
   * Check user exist or not
   */
  def userIsExists(id: String): Boolean
  def userIsExists(user: User): Boolean
  /**
   * Get login infomation if user is logining
   */
  def getLoginInfo(): Option[LoginInfo]
  /**
   * Check login infomation if user is logining
   */
  def checkLogin(id: String, pass: String, path: String): Option[LoginInfo]
  /**
   * Add editform plugin
   */
  def addEditformPlugin(plugin: WikiPlugin, weight: Weight)
  /**
   * Get editform plugin's output
   */
  def getEditformPlugin(): String
  /**
   * Add admin menu will be shown in a case of logining by admin
   */
  def addAdminMenu(label: String, url: String, weight: Weight, desc: String)
  /**
   * Add user menu will be shown always
   */
  def addUserMenu(label: String, url: String, weight: Weight, desc: String)
  /**
   * Get admin menu
   */
  def getAdminMenu(): Menu
  /**
   * Install plugin
   */
  def installPlugin(plugin: WikiPlugin): String
  def installPlugin(pluginName: String): String
  /**
   * Check installed plugin
   */
  def isInstalled(plugin: WikiPlugin): Boolean
  def isInstalled(pluginName: String): Boolean
  /**
   * Add menu, if you don't allow crowling set nofollow true
   */
  def addMenu(name: String, href: String, weight: Weight, nofollow: Boolean)
  /**
   * Register a hook plugin
   * @param name plugin name
   * @param obj  plugin class name
   */
  def addHook(name: String, obj: WikiPlugin)
  /**
   * Execute a registered hook plugin
   * @param name plugin name
   * @param arguments arguments for hook method
   */
  def doHook(name: String, arguments: String*)
  /**
   * Add action handler plugin
   */
  def addHandler(action: String, obj: WikiHandler)
  /**
   * Add action handler plugin for logining user
   */
  def addUserHandler(action: String, obj: WikiHandler)
  /**
   * Add action handler plugin for admin
   */
  def addAdminHandler(action: String, obj: WikiHandler)
  /**
   * Add inline plugin
   */
  @deprecated("This function remains for compatibility for FreeStyleWiki 3.4", "1.0.0")
  def addPlugin(name: String, cls: WikiPlugin)
  def addPlugin(name: String, cls: String)
  /**
   * Register a inline plugin, specify the format "WIKI" or "HTML"
   */
  def addInlinePlugin(name: String, cls: WikiPlugin)
  /**
   * Register a paragraph plugin, specify the format "WIKI" or "HTML"
   */
  def addParagraphPlugin(name: String, cls: WikiPlugin)
  /**
   * Register a block plugin, specify the format "WIKI" or "HTML"
   */
  def addBlockPlugin(name: String, cls: WikiPlugin)
  /**
   * Get plugin info
   */
  def getPluginInfo(name: String): Option[PluginInfo]
  /**
   * Execute registerd ActionHandler
   */
  def callHandler(action: String): String
  /**
   * Convert wiki format string to HTML
   */
  def processWiki(wikiformat: String): String
  /**
   * Call inline plugin or paragragh plugin
   */
  def processPlugin(plugin: WikiPlugin, parser: Parser)
  /**
   * If wiki instance is parsing, return instance of Parser
   */
  def getCurrentParser(): Option[Parser]
  /**
   * Report error from action handler
   */
  def error(message: String): String
  def errorL(message: String): Either[String, play.api.mvc.Result]
  /**
   * Get plugin's instance
   */
  def getPluginInstance[T](cls: T): T
  /**
   * Devine inline plugin to command & arguments
   */
  def parseInlinePlugin(text: String): (String, Array[String], String)
  /**
   * Add format plugin
   */
  def addFormatPlugin(name: String, cls: WikiPlugin)
  /**
   * Get list of format plugins
   */
  def getFormatNames(): List[String]
  /**
   * Convert other format Wiki source to FSWiki style one
   */
  def convertToFswiki(source: String, formatType: WikiFormat, isInline: Boolean): String
  /**
   * Convert FSWiki style source to other format Wiki one
   */
  def convertFromFswiki(source: String, formatType: WikiFormat, isInline: Boolean): String
  /**
   * Get a wiki format edited by current user
   */
  def getEditFormat(): WikiFormat
  /**
   * Add element to head TAG
   */
  def addHeadInfo(info: String)
  /**
   * Freeze a page
   */
  def freezePage(pageName: String)
  /**
   * Un-freeze a page
   */
  def unFreezePage(pageName: String)
  /**
   * Get frozen page list
   */
  def getFreezeList(): List[String]
  /**
   * Check the page is frozen
   */
  def isFreeze(pageName: String): Boolean
  /**
   * Check the page is editable
   */
  def canModifyPage(pageName: String): Boolean
  /**
   * Configure the level of page
   */
  def setPageLevel(pageName: String, level: WikiPageLevel)
  /**
   * Get the level of page
   */
  def getPageLevel(pageName: String): WikiPageLevel
  /**
   * Get the max level for current user's permission
   */
  def getCanShowMax(): Option[WikiPageLevel]
  /**
   * Get the page can be referenced
   */
  def canShow(pageName: String): Boolean
  /**
   * Create page URL for jumping the page
   */
  def createPageUrl(pageName: String): String
  /**
   * Utility method for generating any URL
   */
  def createUrl(params: scala.collection.immutable.HashMap[String, String]): String
  def createUrl(): String
  /**
   * Configure the title in action handler
   */
  def setTitle(title: String, isEditing: Boolean = false)
  /**
   * Get title
   */
  def getTitle(): String
  /**
   * Get page list
   */
  def getPageList(params: String*): List[String]
  /**
   * Get last modified time from data file
   */
  def getLastModified(page: String): DateTime
  /**
   * Get last modified time
   */
  def getLastModifiedLogically(): DateTime
  /**
   * Get page source
   */
  def getPage(pageName: String, format: WikiFormat = WIKI_FORMAT): String
  /**
   * Get backuped source
   * @return None if it not exist
   */
  def getBackup(pageName: String, version: Int): Option[String]
  /**
   * Save wiki page
   */
  def savePage(pageName: String, content: String, updateTimestamp: Boolean = true)
  /**
   * Check the page exists or not
   */
  def pageExists(pageName: String): Boolean
  /**
   * Get CGI object
   */
  def getCGI(): DummyCGI
  /**
   * Redirect to the page specified by arguments
   */
  def redirect(pageName: String, part: Int = 0): Result
  /**
   * Redirect to the page specified by URL
   */
  def redirectURL(url: URL): Result
  def redirectURL(url: String): Result
  /**
   * Get value if key is specified, else set the value
   */
  def config(key: String, value: String)
  def config(key: String): Option[String]
  /**
   * Check the function for farm is enabled or not
   */
  def farmIsEnable(): Boolean
  /**
   * Create child wiki
   */
  def createChildWiki(siteName: String, adminId: String, password: String)
  /**
   * Get current level of wiki;
   * Root => 0, child => 1...and so on.
   */
  def getChildWikiDepth(): Int
  /**
   * Remove a child wiki
   */
  def removeChildWiki(path: String)
  /**
   * Check the child wiki's existence
   */
  def childWikiExists(wikiName: String): Boolean
  /**
   * Get the List of child wiki
   */
  def getWikiList(): List[String]
  /**
   * Get the tree of child wiki
   */
  def searchChild(dir: String): List[String]
  /**
   * Process before the terminate
   */
  def processBeforeExit()

}
