////////////////////////////////////////////////////////////
//
// ページが削除されたときのフック
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
class AttachDelete(className: String, tpe: WikiPluginType, format: WikiFormat)
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
  // ページ削除時に呼び出されるフック関数
  //===========================================================
  def hook(wiki: AbstractWiki, name: String, args: Seq[String]) = {
    val cgi = wiki.getCGI
    val pagename = cgi.getParam("page")
    val encodePage = WikiUtil.urlEncode(pagename)

    val attachDir: File = new File(wiki.config("attach_dir").getOrElse("./attach_dir"))
    attachDir.listFiles.foreach {
      entry =>
        if (entry.getAbsolutePath.contains(s"${encodePage} + ")) {
          new File(attachDir.getName + "/" + entry).delete
        }
    }
    ""
  }
}
