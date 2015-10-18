package jp.gr.java_conf.hangedman.util.wiki

class DefaultStorage {

  // A file saved last updated day of pages
  val MODTIME_FILE = "modtime.dat"
  // A file saved page list
  val PAGE_LIST_FILE = "pagelist.cache"

  def getPage(page: String, path: String): String = {
    ""
  }
  def savePage() = {}
  private def CreatePageListFile() = {}
  private def GetBackupNumber() = {}
  private def RenameOldHistory() = {}
  def getPageList() = {}
  def getLastModified() = {}
  def getLastModified2() = {}
  def pageExists(page: String, path: String): Boolean = {
    true
  }
  def backupType() = {}
  def deleteBackupFiles() = {}
  def getBackupList() = {}
  def getBackup(pageName: String, version: Int = 0): Option[String] = {
    Some("")
  }
  def freezePage() = {}
  def unFreezePage() = {}
  def getFreezeList() = {}
  def isFreeze() = {}
  def setPageLevel() = {}
  def getPageLevel() = {}

}
