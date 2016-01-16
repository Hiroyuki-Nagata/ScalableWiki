////////////////////////////////////////////////////////////
//
// 指定した書籍の書影をamazonから取得して表示し、amazonの書評ページへリンクをはります。
//
////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.amazon

import jp.gr.java_conf.hangedman.plugin._
import jp.gr.java_conf.hangedman.plugin.amazon._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.util.{ Failure, Success, Try }

object Install extends InstallTrait {
  def install(wiki: jp.gr.java_conf.hangedman.util.wiki.AbstractWiki) {
    wiki.addInlinePlugin("amazon", new Amazon("amazonb", Inline, WIKI_FORMAT))
  }
}
