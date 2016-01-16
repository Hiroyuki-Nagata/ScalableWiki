////////////////////////////////////////////////////////////
//
// <p>ToDoリストに項目を追加するためのフォームを出力します。</p>
// <pre>
// {{todoadd ToDo(ToDoを記述したページ、省略可)}}
// </pre>
// <p>
//   フォームに記入して追加を押すと、ToDoリスト用の項目が追加されます。
//   ページ名を省略した場合は、この行の前に追加します。
//   ページ名を指定した場合は、指定したページの最後に追加します。
// </p>
//
////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.todo

import jp.gr.java_conf.hangedman.plugin.todo._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.util.{ Failure, Success, Try }

//===========================================================
// コンストラクタ
//===========================================================
class ToDoAdd(className: String, tpe: WikiPluginType, format: WikiFormat)
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
  // ToDoリスト追加フォーム
  //===========================================================
  def paragraph(wiki: AbstractWiki, dist: String): String = {
    val cgi = wiki.getCGI
    val page = cgi.paramPage("page")

    def form(page: String, dist: String): String = {
      "<form method=\"post\" action=\"" + wiki.createUrl + "\">\n" +
        "優先度：<input type=\"text\" name=\"priority\" size=\"3\"> " +
        "行動：<input type=\"text\" name=\"dothing\" size=\"40\"> " +
        "<input type=\"submit\" value=\"追加\">\n" +
        "<input type=\"hidden\" name=\"action\" value=\"ADD_TODO\">\n" +
        "<input type=\"hidden\" name=\"page\" value=\"" + WikiUtil.escapeHTML(page) + "\">\n" +
        "<input type=\"hidden\" name=\"dist\" value=\"" + WikiUtil.escapeHTML(dist) + "\">\n" +
        "</form>"
    }

    (page, dist) match {
      case (page, _) if page.isEmpty =>
        ""
      case (dist, page) if dist.isEmpty =>
        form(page, dist) // 変数入れ替え
      case (_, dist) if (!wiki.pageExists(dist)) =>
        WikiUtil.paragraphError("distが存在しません。")
      case (page, dist) =>
        form(page, dist)
    }
  }
}
