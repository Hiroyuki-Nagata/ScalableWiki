////////////////////////////////////////////////////////////
//
// ページの名称を変更します。
//
////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.rename

import jp.gr.java_conf.hangedman.plugin._
import jp.gr.java_conf.hangedman.plugin.rename._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.util.{ Failure, Success, Try }

object Install extends InstallTrait {
  def install(wiki: jp.gr.java_conf.hangedman.util.wiki.AbstractWiki) {
    wiki.addEditformPlugin(new RenameForm("rename", EditForm, WIKI_FORMAT), Weight(10))
    wiki.addHandler("RENAME", new RenameHandler("RENAME", EditForm, WIKI_FORMAT))
  }
}
