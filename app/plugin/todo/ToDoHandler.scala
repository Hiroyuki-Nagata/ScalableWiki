//////////////////////////////////////////////////////////////
//
// ToDoプラグインのアクションハンドラ。
// チェックされたToDoを「済」に変更します。
//
//////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.todo

import java.util.regex.Pattern
import jp.gr.java_conf.hangedman.plugin.todo._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.util.{ Failure, Success, Try }

//=============================================================
// コンストラクタ
//=============================================================
class ToDoHandler(className: String, tpe: WikiPluginType, format: WikiFormat)
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

  //=============================================================
  // アクションメソッド
  // ToDoの完了処理
  //=============================================================
  def doAction(wiki: AbstractWiki): play.api.mvc.Result = {
    val cgi = wiki.getCGI
    val buf = new StringBuilder
    val source = cgi.getParam("source")
    val params: Array[String] = cgi.allParameters
    val content = wiki.getPage(source)
    val page = cgi.getParam("page")

    // todoを収集
    val finish = """((^|\n)\*)\s*(\d+)\s+(dothing)(\n|)""".r
    val modifiedContent: String = params.collect {
      case todo if (todo.matches("""^todo\.\d+""")) => todo
    }.map { todo =>
      // メタ文字をクウォート
      Pattern.quote(todo) match {
        case finish(head, index, dothing, tail) =>
          "1 済 3 45"
        case quoted =>
          quoted
      }
    }.mkString("\n")
    wiki.savePage(source, modifiedContent)
    // もともと表示していたページを表示
    wiki.redirect(page)
  }
}
