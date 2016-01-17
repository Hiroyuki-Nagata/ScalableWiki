////////////////////////////////////////////////////////////
//
// Wikiページにファイルを添付するためのプラグインを提供します。
//
////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.attach

import jp.gr.java_conf.hangedman.plugin._
import jp.gr.java_conf.hangedman.plugin.attach._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.collection.immutable.HashMap
import scala.util.{ Failure, Success, Try }

object Install extends InstallTrait {
  def install(wiki: jp.gr.java_conf.hangedman.util.wiki.AbstractWiki) {

    wiki.addHook(
      "initialize",
      new AttachInitializer("initializer", NonSpecify, NO_FORMAT)
    )
    wiki.addHook(
      "remove_wiki",
      new AttachInitializer("initializer", NonSpecify, NO_FORMAT)
    )
    wiki.addHandler(
      "ATTACH",
      new AttachHandler("ATTACH", NonSpecify, NO_FORMAT)
    )
    wiki.addHook(
      "delete",
      new AttachDelete("delete", NonSpecify, NO_FORMAT)
    )
    wiki.addHook(
      "rename",
      new AttachRename("rename", NonSpecify, NO_FORMAT)
    )
    wiki.addInlinePlugin(
      "ref",
      new Ref("ref", Inline, WIKI_FORMAT)
    )
    wiki.addParagraphPlugin(
      "ref_image",
      new RefImage("ref_image", Paragraph, WIKI_FORMAT)
    )
    wiki.addParagraphPlugin(
      "ref_text",
      new RefText("ref_text", Paragraph, WIKI_FORMAT)
    )
    wiki.addParagraphPlugin(
      "files",
      new Files("files", Paragraph, WIKI_FORMAT)
    )
    wiki.addParagraphPlugin(
      "attach",
      new Attach("attach", Paragraph, WIKI_FORMAT)
    )
    wiki.addEditformPlugin(new AttachForm(
      "attach_form", EditForm, WIKI_FORMAT
    ), Weight(50))

    wiki.addAdminMenu(
      "mimeタイプ",
      wiki.createUrl(HashMap("action" -> "ADMINMIME")),
      Weight(990), "MIMEタイプの追加、削除を行います。"
    )

    wiki.addAdminHandler(
      "ADMINMIME",
      new AdminMIMEHandler("ADMINMIME", NonSpecify, NO_FORMAT)
    )
  }
}
