////////////////////////////////////////////////////////////
//
// voteプラグインのアクションハンドラ。
//
////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.vote

import jp.gr.java_conf.hangedman.plugin.vote._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import java.io.File
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import play.Logger
import scala.util.Failure
import scala.util.Success
import scala.util.Try

//===========================================================
// コンストラクタ
//===========================================================
class VoteHandler(className: String, tpe: WikiPluginType, format: WikiFormat)
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
  // 投票の反映
  //===========================================================
  def doAction(wiki: AbstractWiki): play.api.mvc.Result = {
    val cgi = wiki.getCGI
    val item = cgi.getParam("item")
    val votename = cgi.getParam("vote")
    val page = cgi.getParam("page")

    if (page.nonEmpty && votename.nonEmpty && item.nonEmpty) {
      val filename = WikiUtil.makeFilename(
        wiki.config("log_dir").get,
        WikiUtil.urlEncode(votename), "vote"
      )
      WikiUtil.loadConfigHash(wiki, filename).get(item) match {
        case Some(count: String) =>
          val increment: Int = count.toInt + 1
          WikiUtil.saveConfigHash(
            filename,
            WikiUtil.loadConfigHash(wiki, filename).updated(count.toString, increment.toString)
          )
        case None =>
        //
      }
    }
    wiki.redirect(page, 0)
  }
}
