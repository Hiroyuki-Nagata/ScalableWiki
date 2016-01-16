///////////////////////////////////////////////////////////////////////////////
//
// renameプラグインによって呼び出されるrenameフック。
//
///////////////////////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.attach

import jp.gr.java_conf.hangedman.plugin.attach._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.util.{ Failure, Success, Try }

//==============================================================================
// コンストラクタ
//==============================================================================
class AttachRename(className: String, tpe: WikiPluginType, format: WikiFormat)
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

  //==============================================================================
  // フックメソッド（ページのリネーム時に添付ファイルをコピーする）
  //==============================================================================
  def hook(wiki: AbstractWiki, name: String, args: String*): String = {
    val page = wiki.getCGI.getParam("page")
    val newpage = wiki.getCGI.getParam("newpage")
    val dir = wiki.config("attach_dir").getOrElse("./attach_dir")
    val wildcard = String.format("%s/%s.*", dir, WikiUtil.urlEncode(page))

    glob(wildcard).foreach { file =>
      file match {
        case f if (file.matches("""^(.+)\.(.+)""")) =>
          copy(
            String.format("%s/%s.%s", dir, WikiUtil.urlEncode(page), "2"),
            String.format("%s/%s.%s", dir, WikiUtil.urlEncode(newpage), "2")
          ) match {
              case Left(message) =>
                Logger.warn(message)
                WikiUtil.urlDecode("2") + "のコピーに失敗しました。\n\n!"
              case Right(_) =>
                ""
            }
        case _ =>
        // do nothing
      }
    }

    ""
  }
}
