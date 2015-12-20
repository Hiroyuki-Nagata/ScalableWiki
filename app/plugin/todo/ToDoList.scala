//////////////////////////////////////////////////////////////
//
// <p>ToDoリストを表示します。</p>
// <p>
//   まず適当なページにToDoを記述します。ToDoの記述は以下のような感じです。
// </p>
// <pre>
// * 22(優先度) トイレットペーパーを買う(行動)
// </pre>
// <p>
//   優先度と行動の間は空白を一つ以上空けてください。
//   プラグインの使い方は以下のようになります。
// </p>
// <pre>
// {{todolist ToDo(ToDoを記述したページ),5(表示する件数、省略可)}}
// </pre>
// <p>
//   優先度の高い順に上から表示されます。
//   alwaysオプションをつけるとチェックボックスと完了ボタンが表示され、
//   ToDoが完了したらチェックボックスにチェックを入れて「完了」を押すと
//   ToDoを記述したページでは
// </p>
// <pre>
// * 済 22 トイレットペーパーを買う
// </pre>
// <p>
//   のように変更されtodolistから外されます。
//   なお、alwaysオプションをつけていない場合でも、
//   管理者としてログインすれば同様のフォームが表示されます。
// </p>
//
//////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.todo

import jp.gr.java_conf.hangedman.plugin.todo._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.collection.immutable.ListMap
import scala.util.{ Failure, Success, Try }

//=============================================================
// コンストラクタ
//=============================================================
class ToDoList(className: String, tpe: WikiPluginType, format: WikiFormat)
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

  //=============================================================
  // パラグラフメソッド
  //=============================================================
  def paragraph(wiki: AbstractWiki, source: String, count: String, option: String): String = {
    val cgi = wiki.getCGI
    val page = cgi.paramPage("page")
    val buf = new StringBuilder

    if (source.isEmpty) {
      WikiUtil.paragraphError("ページを指定してください。")
    } else if (wiki.pageExists(source)) {
      WikiUtil.paragraphError("sourceが存在しません。")
    } else if (wiki.canShow(source)) {
      WikiUtil.paragraphError("ページの参照権がありません。")
    }

    val content = wiki.getPage(source)
    val lines: Array[String] = content.split('\n')

    // 書式からtodoを抽出
    val todoRegex = """^\*\s*(\d+)\s+(.*)""".r
    val todolist: ListMap[Int, String] = ListMap(lines.map { line =>
      line match {
        case todoRegex(priority, dothing) =>
          priority.toInt -> dothing
      }
      // 優先順位でソート
    }.toSeq.sortBy(_._1): _*)

    val countable: Int = Try { count.toInt } match {
      case Success(c) =>
        c
      case Failure(e) =>
        todolist.size
    }

    // リスト表示 + 完了フォーム
    val login = wiki.getLoginInfo()
    if (count == "always" || option == "always" || login.isDefined) {
      buf.append("<div class=\"todo\">"
        + "<form action=\"" + wiki.createUrl + "\" method=\"POST\">\n"
        + "<input type=\"hidden\" name=\"source\" value=\"" + WikiUtil.escapeHTML(source) + "\">\n"
        + "<input type=\"hidden\" name=\"page\" value=\"" + WikiUtil.escapeHTML(page) + "\">\n"
        + "<input type=\"hidden\" name=\"action\" value=\"FINISH_TODO\">")
    }
    buf.append("<ol>\n")

    todolist.zipWithIndex.takeWhile {
      todo => todo._2 <= countable
    }.map(tuple => tuple._1).foreach {
      case (priority: Int, dothing: String) =>
        val value = WikiUtil.escapeHTML(dothing)
        val content = wiki.processWiki(dothing).replaceAll("""<\/?p>""", "")
        buf.append("<li value=\"%s\">".format(priority.toString))
        if (count == "always" || option == "always" || login.isDefined) {
          buf.append("<input name=\"todo.i\" type=\"checkbox\" value=\"value\">" + content + "</input></li>\n")
        } else {
          buf.append(content + "</li>\n")
        }
    }

    buf.append("</ol>")
    if (count == "always" || option == "always" || login.isDefined) {
      buf.append("<input type=\"submit\" value=\"完了\"></form></div>")
    }
    buf.result
  }
}
