////////////////////////////////////////////////////////////
//
// 編集画面に添付ファイルフォームを表示するプラグイン
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

//==========================================================
// コンストラクタ
//==========================================================
class AttachForm(className: String, tpe: WikiPluginType, format: WikiFormat)
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

  //==========================================================
  // 添付フォームを表示
  //==========================================================
  def editform(wiki: AbstractWiki): String = {

    // ページが存在する場合だけフォームを表示
    if (wiki.pageExists(wiki.getCGI.getParam("page"))) {
      "<h2>添付ファイル</h2>\n" +
        "<p>" + wiki.processWiki("{{\"files\"}}") + "</p>\n" +
        wiki.processWiki("{{\"attach\"}}")
    } else {
      ""
    }
  }
}
