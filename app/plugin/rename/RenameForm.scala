////////////////////////////////////////////////////////////
//
// ページの名称を変更するためのフォームを出力します。
//
////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.rename

import jp.gr.java_conf.hangedman.plugin.rename._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.util.{ Failure, Success, Try }

//===========================================================
// コンストラクタ
//===========================================================
class RenameForm(className: String, tpe: WikiPluginType, format: WikiFormat)
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

  //===========================================================
  // ヘルプを表示します。
  //===========================================================
  def editform(wiki: AbstractWiki): String = {
    val cgi = wiki.getCGI
    val page = cgi.paramPage("page")

    // ページが存在する場合だけフォームを表示
    if (wiki.pageExists(page)) {
      val time = wiki.getLastModified(page).toString()
      "<h2>リネーム・コピー</h2>" +
        "<form method=\"post\" action=\"" + wiki.createUrl + "\">\n" +
        "  <input type=\"text\" name=\"newpage\" size=\"40\" value=\"" + WikiUtil.escapeHTML(page) + "\">\n" +
        "  <br>\n" +
        "  <input type=\"radio\"  id=\"do_move\" name=\"do\" value=\"move\" checked><label for=\"do_move\">リネーム</label>\n" +
        "  <input type=\"radio\"  id=\"do_movewm\" name=\"do\" value=\"movewm\"><label for=\"do_movewm\">メッセージを残してリネーム</label>\n" +
        "  <input type=\"radio\"  id=\"do_copy\" name=\"do\" value=\"copy\"><label for=\"do_copy\">コピー</label>\n" +
        "  <input type=\"submit\" name=\"execute_rename\" value=\" 実行 \">\n" +
        "  <input type=\"hidden\" name=\"action\" value=\"RENAME\">" +
        "  <input type=\"hidden\" name=\"lastmodified\" value=\"" + WikiUtil.escapeHTML(time) + "\">\n" +
        "  <input type=\"hidden\" name=\"page\" value=\"" + WikiUtil.escapeHTML(page) + "\">" +
        "</form>\n"
    } else {
      ""
    }
  }
}
