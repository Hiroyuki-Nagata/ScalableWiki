package jp.gr.java_conf.hangedman.util.wiki

import java.io.File
import jp.gr.java_conf.hangedman.model.WikiPageLevel
import jp.gr.java_conf.hangedman.util.WikiUtil

/**
 * FSWikiデフォルトのストレージプラグイン。
 *
 * setup.datのbackup=1もしくはbackupディレクティブを省略した場合は１世代のみ、
 * backup=2以上もしくは0を指定した場合は世代バックアップに対応します。
 * backup=0を指定した場合は無制限にバックアップを行います。
 */
class DefaultStorage(abstractWiki: AbstractWiki) {

  // A file saved last updated day of pages
  val MODTIME_FILE = "modtime.dat"
  // A file saved page list
  val PAGE_LIST_FILE = "pagelist.cache"

  val wiki = abstractWiki
  val backup: String = wiki.config("backup") match {
    case Some(backup) =>
      backup
    case None =>
      "1"
  }
  /**
   * ページを取得
   */
  def getPage(page: String, path: String): String = {
    val dir = if (path.nonEmpty) {
      wiki.config("data_dir").getOrElse("./data_dir") + s"/${path}"
    } else {
      wiki.config("data_dir").getOrElse("./data_dir")
    }

    val filename = WikiUtil.makeFilename(dir, WikiUtil.urlEncode(page), "wiki")
    val file: File = new File(filename)
    if (file.exists) {
      scala.io.Source.fromFile(file).mkString("")
    } else {
      ""
    }
  }
  /**
   * ページを保存
   */
  def savePage(rawPage: String, rawContent: String, sage: Boolean) = {
    // adjust pagename and contents
    val page = WikiUtil.trim(rawPage)
  }
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
  def isFreeze(page: String, path: String): Boolean = {
    false
  }
  def setPageLevel(pageName: String, level: WikiPageLevel) = {
  }
  def getPageLevel() = {}

}
