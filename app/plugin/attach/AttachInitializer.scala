////////////////////////////////////////////////////////////
//
// attachプラグインの初期化およびWikiFarmによるWiki削除時
// の処理を行うフックプラグイン
//
////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.attach

import java.io.File
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
class AttachInitializer(className: String, tpe: WikiPluginType, format: WikiFormat)
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
  // attachプラグインの初期化
  //===========================================================
  def hook(wiki: AbstractWiki, name: String, args: Seq[String]): String = {
    // remove_wikiフック
    if (name == "remove_wiki") {
      val path = wiki.getCGI.getParam("path")
      wiki.config("attach_dir") match {
        case Some(attachDir) if (new File(attachDir).exists) =>
          WikiUtil.rmtree(new File(attachDir))
          ""
        case _ => // do nothing
          ""
      }
      // initializeフック
    } else if (name == "initialize") {
      // Farmで動作している場合はグローバル変数を上書き
      val pathInfo = wiki.getCGI.pathInfo
      wiki.config("attach_dir") match {
        case Some(attachDir) if (pathInfo.length > 0) =>
          wiki.config("attach_dir", attachDir + pathInfo.toString)
        case _ => // do nothing
      }
      wiki.config("attach_dir") match {
        case Some(attachDir) if (!new File(attachDir).exists) =>
          new File(attachDir).mkdirs
          ""
        case _ => // do nothing
          ""
      }
    } else {
      ""
    }
  }
}
