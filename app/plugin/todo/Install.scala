///////////////////////////////////////////////////////////////////////////////
//
// Wikiページ上でTODOの管理を行うためのプラグインを提供します。
//
//////////////////////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.todo

import jp.gr.java_conf.hangedman.plugin.todo._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.util.{ Failure, Success, Try }

object Install {
  def install(wiki: jp.gr.java_conf.hangedman.util.wiki.AbstractWiki) {
    wiki.addParagraphPlugin("todolist", new ToDoList("todolist", Paragraph, WIKI_FORMAT))
    wiki.addHandler[ToDoHandler]("FINISH_TODO", new ToDoHandler("FINISH_TODO", Paragraph, WIKI_FORMAT))
    wiki.addParagraphPlugin("todoadd", new ToDoAdd("todoadd", Paragraph, WIKI_FORMAT))
    wiki.addHandler[ToDoAddHandler]("ADD_TODO", new ToDoAddHandler("ADD_TODO", Paragraph, WIKI_FORMAT))
  }
}
