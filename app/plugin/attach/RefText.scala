////////////////////////////////////////////////////////////
//
// <p>添付したテキストファイルを表示します。</p>
// <pre>
// {{ref_text ファイル名}}
// </pre>
// <p>別のページに添付されたファイルを参照することもできます。</p>
// <pre>
// {{ref_text ファイル名,ページ名}}
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
class RefText(className: String, tpe: WikiPluginType, format: WikiFormat)
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
  // パラグラフメソッド
  //===========================================================
  def paragraph(wiki: AbstractWiki, file: String, page: String): String = {

    if (file.isEmpty) {
      WikiUtil.paragraphError("ファイルが指定されていません。", "WIKI")
    }

    val encodedPage = WikiUtil.urlEncode(if (page.isEmpty) {
      wiki.getCGI.getParam("page")
    } else {
      page
    })

    if (!wiki.canShow(page)) {
      WikiUtil.paragraphError("ページの参照権限がありません。", "WIKI")
    }

    val filename = wiki.config("attach_dir") match {
      case Some(attachDir) =>
        attachDir + "/" + encodedPage + " + " + WikiUtil.urlEncode(file)
      case None =>
        "./attach_dir/" + encodedPage + " + " + WikiUtil.urlEncode(file)
    }
    if (!new File(filename).exists) {
      WikiUtil.paragraphError("ファイルが存在しません。", "WIKI")
    } else {
      scala.io.Source.fromFile(file).getLines.toList.map {
        line => s" ${line}"
      }.mkString("\n")
    }
  }
}
