////////////////////////////////////////////////////////////
//
// <p>添付した画像ファイルを表示します。</p>
// <pre>
// {{ref_image ファイル名}}
// </pre>
// <p>
// オプションで画像のサイズを指定することができます。
// 以下の例では幅650ピクセル、高さ400ピクセルで画像を表示します。
// </p>
// <pre>
// {{ref_image ファイル名,w650,h400}}
// </pre>
// <p>別のページに添付されたファイルを参照することもできます。</p>
// <pre>
// {{ref_image ファイル名,ページ名}}
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
class RefImage(className: String, tpe: WikiPluginType, format: WikiFormat)
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

  def hook(wiki: AbstractWiki, name: String, args: String*) = { "" }

  //===========================================================
  // パラグラフメソッド
  //===========================================================
  def paragraph(wiki: AbstractWiki, file: String, options: Array[String]): String = {

    var page = ""
    var width = 0
    var height = 0

    if (file.isEmpty) {
      WikiUtil.paragraphError("ファイルが指定されていません。", "WIKI")
    }
    // あまり良くない書き方 FIXME
    options.foreach { option =>
      if (option.matches("""^w([0-9]+)""")) {
        width = 1
      } else if (option.matches("""^h([0-9]+)""")) {
        height = 1
      } else {
        page = option
      }
    }
    if (page.isEmpty) {
      page = wiki.getCGI.getParam("page")
    }
    if (!wiki.canShow(page)) {
      WikiUtil.paragraphError("ページの参照権限がありません。", "WIKI")
    }

    val filename = wiki.config("attach_dir") match {
      case Some(attachDir) =>
        attachDir + "/" + page + " + " + WikiUtil.urlEncode(file)
      case None =>
        "./attach_dir/" + page + " + " + WikiUtil.urlEncode(file)
    }

    wiki.getCurrentParser.fold(WikiUtil.paragraphError("ページの参照権限がありません。", "WIKI")) {
      parser => parser.lImage(page, filename, width, height)
    }
  }
}
