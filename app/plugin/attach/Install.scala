////////////////////////////////////////////////////////////
//
// Wikiページにファイルを添付するためのプラグインを提供します。
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

object Install {
  def install(wiki: jp.gr.java_conf.hangedman.util.wiki.AbstractWiki) {

    wiki.addHook[AttachInitializer](
      "initialize",
      new AttachInitializer("initializer", NonSpecify, NO_FORMAT)
    )
    wiki.addHook[AttachInitializer](
      "remove_wiki",
      new AttachInitializer("initializer", NonSpecify, NO_FORMAT)
    )
    wiki.addHandler[AttachHandler](
      "ATTACH",
      new AttachHandler("ATTACH", NonSpecify, NO_FORMAT)
    )
    wiki.addHook[AttachDelete](
      "delete",
      new AttachDelete("delete", NonSpecify, NO_FORMAT)
    )
    wiki.addHook[AttachRename](
      "rename",
      new AttachRename("rename", NonSpecify, NO_FORMAT)
    )
    /**
     * wiki.addInlinePlugin("ref", Ref)
     */
    wiki.addParagraphPlugin(
      "ref_image",
      new RefImage("ref_image", Paragraph, WIKI_FORMAT)
    )
    wiki.addParagraphPlugin(
      "ref_text",
      new RefText("ref_text", Paragraph, WIKI_FORMAT)
    )
    /**
     *
     * wiki.addParagraphPlugin("files", Files)
     * wiki.addParagraphPlugin("attach", Attach)
     * wiki.addEditformPlugin("plugin::attach::AttachForm", 50)
     *
     * wiki.addAdminMenu("mimeタイプ", wiki.createUrl({ action => "ADMINMIME" }), 990,
     * "MIMEタイプの追加、削除を行います。")
     *
     * wiki.addAdminHandler("ADMINMIME", "plugin::attach::AdminMIMEHandler")
     */
  }
}
