////////////////////////////////////////////////////////////
//
// <p>添付ファイルへのアンカを表示します。</p>
// <pre>
// {{ref ファイル名}}
// </pre>
// <p>別のページに添付されたファイルを参照することもできます。</p>
// <pre>
// {{ref ファイル名,ページ名}}
// </pre>
// <p>
//   通常はアンカとしてファイル名が表示されますが、
//   別名として任意の文字列を表示することもできます。
// </p>
// <pre>
// {{ref ファイル名,ページ名,別名}}
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
class Ref(className: String, tpe: WikiPluginType, format: WikiFormat)
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
  // インライン関数
  //===========================================================
  def inline(wiki: AbstractWiki, file: String, page: String, alias: String): String = {
    if (file.isEmpty) {
      WikiUtil.inlineError("ファイルが指定されていません。")
    }
    if (!wiki.canShow(page)) {
      WikiUtil.paragraphError("ページの参照権限がありません。", "WIKI")
    }

    wiki.config("attachDir") match {
      case None =>
        WikiUtil.inlineError("ファイルが存在しません。")
      case Some(attachDir) =>
        val filename: String = attachDir + "/" + WikiUtil.urlEncode(file)
        if (new File(filename).exists) {
          val buf = new StringBuilder
          buf.append("<a href=\"" +
            wiki.createUrl(HashMap("action" -> "attach", "page" -> page, "file" -> file)) +
            "\">" +
            WikiUtil.escapeHTML(if (alias.isEmpty) file else alias) +
            "</a>")

          // ダウンロード回数を取得
          val countFile: String = wiki.config("log_dir").getOrElse("./log") + "/" +
            wiki.config("download_count_file").getOrElse("count")

          WikiUtil.loadConfigHash(wiki, countFile).get(s"${page}::${file}") match {
            case Some(currentNumber) =>
              Try { currentNumber.trim.toInt } match {
                case Success(c) =>
                  buf.append("(${c})")
                case Failure(e) =>
                  buf.append("(0)")
              }
            case None =>
              buf.append("(0)")
          }
          buf.result
        } else {
          WikiUtil.inlineError("ファイルが存在しません。")
        }
    }
  }
}
