package jp.gr.java_conf.hangedman.util.wiki

import java.io.File
import jp.gr.java_conf.hangedman.model.WikiPageLevel
import jp.gr.java_conf.hangedman.util.WikiUtil

sealed abstract class PageFlag
case object Create extends PageFlag
case object Update extends PageFlag
case object Remove extends PageFlag

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
  val showLevel = scala.collection.mutable.HashMap[String, String]().empty

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
  /**
   * ページ一覧のインデックスファイルを作成、更新します。
   * 第一引数にページ名、第二引数に'create'、'update'、'remove'のいずれかを指定します。
   * インデックスファイルが存在しない場合は引数に関わらずインデックスファイルの作成を行います。
   */
  private def createPageListFile(page: String, flag: PageFlag) = {
  }
  /**
   * ページを保存
   */
  private def getBackupNumber() = {
  }
  /**
   * ページを保存
   */
  private def renameOldHistory() = {
  }
  /**
   * ページを保存
   */
  def getPageList() = {}
  /**
   * ページを保存
   */
  def getLastModified() = {}
  /**
   * ページを保存
   */
  def getLastModified2() = {}
  /**
   * ページを保存
   */
  def pageExists(page: String, path: String): Boolean = {
    true
  }
  /**
   * ページを保存
   */
  def backupType() = {}
  /**
   * ページを保存
   */
  def deleteBackupFiles() = {}
  /**
   * ページを保存
   */
  def getBackupList() = {}
  /**
   * ページを保存
   */
  def getBackup(pageName: String, version: Int = 0): Option[String] = {
    Some("")
  }
  /**
   * ページを保存
   */
  def freezePage() = {}
  /**
   * ページを保存
   */
  def unFreezePage() = {}
  /**
   * ページを保存
   */
  def getFreezeList() = {}
  /**
   * ページを保存
   */
  def isFreeze(page: String, path: String): Boolean = {
    false
  }
  /**
   * ページを保存
   */
  def setPageLevel(pageName: String, level: WikiPageLevel) = {
  }
  /**
   * Get page level of reference
   */
  def getPageLevel(page: String, path: String) = {
    if (!showLevel.get(path).isDefined) {
      // execute replacing config_dir
      val configdir = wiki.config("config_dir").getOrElse("./config_dir")
      if (path.nonEmpty) {
        wiki.config("config_dir", s"${configdir}/${path}")
      }
    }
  }
}
