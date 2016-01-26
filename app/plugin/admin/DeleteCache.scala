///////////////////////////////////////////////////////////////////////////////
//
// キーワードキャッシュを削除します。
//
///////////////////////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.admin

import jp.gr.java_conf.hangedman.plugin._
import jp.gr.java_conf.hangedman.plugin.admin._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.util.{ Failure, Success, Try }

//==============================================================================
// コンストラクタ
//==============================================================================
class DeleteCache(className: String, tpe: WikiPluginType, format: WikiFormat)
    extends WikiHandler(className, tpe, format) {

  //===========================================================
  // installメソッド
  //===========================================================
  def install(wiki: AbstractWiki): Either[String, Boolean] = {
    Try {
      //Install.install(wiki)
    } match {
      case Success(_) =>
        Right(true)
      case Failure(e) =>
        Logger.error(e.getMessage, e)
        Left(e.getMessage)
    }
  }

  def doAction(wiki: AbstractWiki): Either[String, play.api.mvc.Result] = {
    Left("")
  }

  //==============================================================================
  // フック
  //==============================================================================
  def hook(wiki: AbstractWiki): String = {

    val logDir = wiki.config("log_dir").getOrElse("./log_dir")
    val cachefiles = Seq("keywords2.cache", "keywords.cache")

    // キャッシュファイルがあれば削除。
    cachefiles.foreach { cache =>
      val cachefile = logDir + "/" + cache
      if (new File(cachefile).exists)
        new File(cachefile).delete
    }
    ""
  }
}
