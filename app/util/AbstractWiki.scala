package jp.gr.java_conf.hangedman.util

import jp.gr.java_conf.hangedman.model._

abstract class AbstractWiki {

  /*
   * Add user; specify 0 if user is admin, else 1
   */
  def addUser(id: String, pass: String, role: Role)
  def addUser(user: User)
  /*
   * Check user exist or not
   */
  def userIsExists(id: String): Boolean
  def userIsExists(user: User): Boolean
  /*
   * Get login infomation if user is logining
   */
  def getLoginInfo(): Option[LoginInfo]
  /*
   * Check login infomation if user is logining
   */
  def checkLogin(id: String, pass: String, path: String): Option[LoginInfo]
  /*
   * Add editform plugin
   */
  def addEditformPlugin(plugin: WikiPlugin, weight: Weight)
  /*
   * Get editform plugin's output
   */
  def getEditformPlugin(): String
  /*
   * Add admin menu will be shown in a case of logining by admin
   */
  def addAdminMenu(label: String, url: String, weight: Weight, desc: String)
  /*
   * Add user menu will be shown always
   */
  def addUserMenu(label: String, url: String, weight: Weight, desc: String)
  /*
   * Get admin menu
   */
  def getAdminMenu(): Menu
  /*
   * Install plugin
   */
  def installPlugin(plugin: WikiPlugin)
  /*
   * Check installed plugin
   */
  def isInstalled(plugin: WikiPlugin): Boolean

  def addMenu()
  def addHook()
  def doHook()
  def addHandler()
  def addUserHandler()
  def addAdminHandler()
  def addPlugin()
  def addInlinePlugin()
  def addParagraphPlugin()
  def addBlockPlugin()
  def getPluginInfo()
  def callHandler()
  def processWiki()
  def processPlugin()
  def getCurrentParser()
  def doAction()
  def error()
  def getPluginInstance()
  def parseInlinePlugin()
  def addFormatPlugin()
  def getFormatNames()
  def convertToFswiki()
  def convertFromFswiki()
  def getEditFormat()
  def addHeadInfo()
  def freezePage()
  def unFreezePage()
  def getFreezeList()
  def isFreeze()
  def canModifyPage()
  def setPageLevel()
  def getPageLevel()
  def GetCanShowMax()
  def canShow()
  def createPageUrl()
  def createUrl()
  def setTitle()
  def getTitle()
  def getPageList()
  def getLastModified()
  def getLastModified2()
  def getPage()
  def getBackup()
  def savePage()
  def pageExists()
  def getCGI()
  def redirect()
  def redirectURL()
  def config()
  def farmIsEnable()
  def createWiki()
  def GetWikiDepth()
  def removeWiki()
  def wikiExists()
  def getWikiList()
  def searchChild()
  def processBeforeExit()

}
