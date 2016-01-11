////////////////////////////////////////////////////////////
//
// <p>簡易的な投票フォームと途中経過を表示します。</p>
// <pre>
// {{vote 投票名,項目1,項目2,}}
// </pre>
// <p>
//   例えば以下のように使用します。
//   第一引数にはその投票を示すわかりやすい名前をつけてください。
//   第二引数以降が実際に表示される選択項目になります。
// </p>
// <pre>
// {{vote FSWikiの感想,よい,普通,ダメ}}
// </pre>
//
////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.vote

import jp.gr.java_conf.hangedman.plugin.vote._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import java.io.File
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import play.Logger
import scala.collection.immutable.HashMap
import scala.util.Failure
import scala.util.Success
import scala.util.Try

//===========================================================
// コンストラクタ
//===========================================================
class Vote(className: String, tpe: WikiPluginType, format: WikiFormat)
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
  // 投票フォーム
  //===========================================================
  def paragraph(wiki: AbstractWiki, votename: String, itemlist: Array[String]): String = {
    val cgi = wiki.getCGI
    val page = cgi.paramPage("page")

    // 引数のエラーチェック
    if (votename.isEmpty) {
      WikiUtil.paragraphError("投票名が指定されていません。", "Wiki")
    }
    if (itemlist.isEmpty) {
      WikiUtil.paragraphError("項目名が指定されていません。", "Wiki")
    }

    // 読み込む
    val filename =
      WikiUtil.makeFilename(
        wiki.config("log_dir").get,
        WikiUtil.urlEncode(votename),
        "vote"
      )
    val hash = WikiUtil.loadConfigHash(wiki, filename)

    // 表示用テキストを組み立てる
    val buf = new StringBuilder(",項目,得票数\n")

    itemlist.map { item =>
      val count = hash.get(item).getOrElse("0")
      buf.append(",item,count票 - [投票|" +
        wiki.createUrl(HashMap(
          "page" -> page,
          "vote" -> votename,
          "item" -> item,
          "action" -> "VOTE"
        )) + "]\n")
    }
    buf.result
  }
}
