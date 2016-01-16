////////////////////////////////////////////////////////////
//
// <p>そのページに添付されているファイルを一覧表示します。</p>
// <p>FooterやMenuに記述しておくと便利です。</p>
// <pre>
// {{files}}
// </pre>
// <p>Menuに記述する場合など、vオプションをつけると縦に表示することができます。</p>
// <pre>
// {{files v}}
// </pre>
//
////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.attach

import jp.gr.java_conf.hangedman.plugin.attach._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.collection.immutable.HashMap
import scala.util.{ Failure, Success, Try }

//===========================================================
// コンストラクタ
//===========================================================
class Files(className: String, tpe: WikiPluginType, format: WikiFormat)
    extends WikiPlugin(className, tpe, format) {

  //===========================================================
  // installメソッド
  //===========================================================
  def install(wiki: AbstractWiki): Either[String, Boolean] = {
    Try {
      Install.install(wiki)
    } match {
      case Success(_) =>
        Right(true)
      case Failure(e) =>
        Logger.error(e.getMessage, e)
        Left(e.getMessage)
    }
  }

  def hook(wiki: AbstractWiki, name: String, args: Seq[String]) = { "" }

  //===========================================================
  // 添付ファイルの一覧を表示するインライン関数
  //===========================================================
  def paragraph(wiki: AbstractWiki, way: String = ""): String = {
    val cgi = wiki.getCGI
    val pagename = cgi.getParam("page")

    // 参照権があるかどうか調べる
    if (!wiki.canShow(pagename)) {
      ""
    }

    val buf = new StringBuilder
    val editFlag = canAttachDelete(wiki, pagename)

    if (way == "V" || way == "v") {
      buf.append("<ul>\n")
    }

    getFileList(wiki, pagename).foreach { file: String =>
      if (way == "V" || way == "v") {
        buf.append(buf.result +
          "<li><a href=\"" +
          wiki.createUrl(HashMap("action" -> "ATTACH", "page" -> pagename, "file" -> file)) +
          "\">" +
          WikiUtil.escapeHTML(file) +
          "</a>")
      } else {
        buf.append(buf.result +
          "<a href=\"" +
          wiki.createUrl(HashMap("action" -> "ATTACH", "page" -> pagename, "file" -> file)) +
          "\">" +
          WikiUtil.escapeHTML(file) +
          "</a>")

        if (editFlag) {
          buf.append("[<a href=\"" +
            wiki.createUrl(
              HashMap("action" -> "ATTACH", "CONFIRM" -> "yes", "page" -> pagename, "file" -> file)
            ) +
              "\">削除</a>]")
        }

        if (way == "V" || way == "v") {
          buf.append("</li>\n")
        } else {
          buf.append("\n")
        }
      }
    }

    if (way == "V" || way == "v") {
      buf.append("</ul>\n")
    }

    buf.result
  }

  //===========================================================
  // ファイルの一覧を取得する関数
  //===========================================================
  def getFileList(wiki: AbstractWiki, page: String): List[String] = {
    val encodePage = WikiUtil.urlEncode(page)

    wiki.config("attach_dir") match {
      case None =>
        List.empty[String]
      case Some(attachDir) =>
        val attachDirs: File = new File(attachDir)
        if (attachDirs.exists && attachDirs.listFiles.exists(file => file.getName == s"${encodePage}.")) {
          val list = attachDirs.listFiles.filter {
            file => file.getName == s"${encodePage}."
          } map {
            file: File => WikiUtil.urlDecode(file.getName.split('.')(1))
          }
          list.toList
        } else {
          List.empty[String]
        }
    }
  }

  //===========================================================
  // 添付ファイルが削除可能かどうか判定する関数
  //===========================================================
  def canAttachDelete(wiki: AbstractWiki, page: String): Boolean = {

    val login = wiki.getLoginInfo
    val config = wiki.config("accept_attach_delete").getOrElse(0)

    if (!wiki.canModifyPage(page)) {
      false
    } else if (config == 0 && login.isDefined) {
      false
    } else if (config == 2 && (login.isDefined /** || login["type"] != 0) */ )) {
      false
    }

    true
  }

  //===========================================================
  // 添付ファイルが更新可能かどうか判定する関数
  //===========================================================
  def canAttachUpdate(wiki: AbstractWiki, page: String): Boolean = {
    val login = wiki.getLoginInfo()
    val config = wiki.config("accept_attach_update").getOrElse(1)

    if (!wiki.canModifyPage(page)) {
      false
    } else if (config == 1 && !login.isDefined) {
      false
    } else if (config == 2 && (!login.isDefined /**|| login.("type")!=0)*/ )) {
      false
    }

    true
  }
}
