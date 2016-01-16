////////////////////////////////////////////////////////////
//
// <p>ファイルを添付するためのフォームを表示します。</p>
// <pre>
// {{attach}}
// </pre>
// <p>
//   添付したファイルはフォームの上に一覧表示されます。
//   同じファイルを添付すると複数表示されてしまうのはご愛嬌です。
//   nolistオプションをつけると一覧表示を行いません。
// </p>
// <pre>
// {{attach nolist}}
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
import scala.util.{ Failure, Success, Try }

//===========================================================
// コンストラクタ
//===========================================================
class Attach(className: String, tpe: WikiPluginType, format: WikiFormat)
    extends WikiPlugin(className, tpe, format) {

  // ページ
  var pages = collection.mutable.Map[String, Int]().withDefaultValue(0)

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
  // 添付フォームの表示
  //===========================================================
  def paragraph(wiki: AbstractWiki, option: String): String = {

    val cgi = wiki.getCGI
    val page = cgi.getParam("page")

    if (option.isEmpty || option != "nolist") {
      pages("page") += (1 + pages("page"))
    } else {
      pages("page") += 0
    }

    val buf = new StringBuilder("<form action=\"" +
      wiki.createUrl +
      "\" method=\"post\" enctype=\"multipart/form-data\">\n" +
      "  <input type=\"file\" name=\"file\">\n" +
      "  <input type=\"submit\" name=\"UPLOAD\" value=\" 添 付 \">\n" +
      "  <input type=\"hidden\" name=\"page\" value=\"" +
      WikiUtil.escapeHTML(page) + "\">\n" +
      "  <input type=\"hidden\" name=\"action\" value=\"ATTACH\">\n")

    if (pages("page") != 0) {
      buf.append("  <input type=\"hidden\" name=\"count\" value=\"" + pages("page") + "\">\n")
    }

    buf.append("</form>\n")
    buf.result
  }
}
