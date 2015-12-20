////////////////////////////////////////////////////////////
//
// 簡易投票フォームの表示を行います。
//
////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.vote

import jp.gr.java_conf.hangedman.model.Paragraph
import jp.gr.java_conf.hangedman.model.WIKI_FORMAT
import jp.gr.java_conf.hangedman.plugin.vote._
import jp.gr.java_conf.hangedman.util.WikiUtil
import java.io.File

object Install {
  def install(wiki: jp.gr.java_conf.hangedman.util.wiki.AbstractWiki) {
    wiki.addParagraphPlugin("vote", new Vote("vote", Paragraph, WIKI_FORMAT))
    wiki.addHandler[VoteHandler]("VOTE", new VoteHandler("VOTE", Paragraph, WIKI_FORMAT))
  }
}
