////////////////////////////////////////////////////////////
//
// ToDoAddプラグインのアクションハンドラ。
// ToDoを追加します。
//
////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.todo

import jp.gr.java_conf.hangedman.plugin.todo._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.util.{ Failure, Success, Try }

//===========================================================
// コンストラクタ
//===========================================================
class ToDoAddHandler(className: String, tpe: WikiPluginType, format: WikiFormat)
    extends WikiHandler(className, tpe, format) {

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
  // ToDoの追加
  //===========================================================
  def doAction(wiki: AbstractWiki): Either[String, play.api.mvc.Result] = {
    val cgi = wiki.getCGI
    val dist = cgi.getParam("dist")
    val page = cgi.getParam("page")

    // フォーマットプラグインへの対応
    val format = wiki.getEditFormat
    val priority = wiki.convertToFswiki(cgi.getParam("priority"), format, true)
    val dothing = wiki.convertToFswiki(cgi.getParam("dothing"), format, true)

    if (priority.matches("""\d+""") && dothing.nonEmpty && dist.nonEmpty) {
      val content = wiki.getPage(dist)
      // content =~ s / (^|\n) \*\ s *\ d +\ s +\ Qdothing \ E(\ n |) / 1 * priority dothing2 /
      // or
      // content =~ s / (^|.*\n)(\ Q { ("todoadd") } \ E) / 1 * priority dothing \ n2 /
      // or
      // content =~ s / (\ n ?) /\ n * priority dothing1 /
      wiki.savePage(dist, content)
      wiki.doHook("save_end")
    }
    Right(wiki.redirect(page, 0))
  }
}
