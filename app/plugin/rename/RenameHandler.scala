///////////////////////////////////////////////////////////////////////////////
//
// ページ名称の変更・ページのコピーをするハンドラ。
// 処理前にrenameフックを呼び出します。
//
///////////////////////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.rename

import jp.gr.java_conf.hangedman.plugin.rename._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import org.joda.time.DateTime
import play.Logger
import scala.util.{ Failure, Success, Try }

//==============================================================================
// コンストラクタ
//==============================================================================
class RenameHandler(className: String, tpe: WikiPluginType, format: WikiFormat)
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

  //==============================================================================
  // アクションの実行
  //==============================================================================
  def doAction(wiki: AbstractWiki): String = {
    doRename(wiki)
  }

  //==============================================================================
  // リネームを実行
  //==============================================================================
  def doRename(wiki: AbstractWiki): String = {
    val cgi = wiki.getCGI
    val pagename = cgi.getParam("page")
    val newpagename = cgi.getParam("newpage")
    val paramDo = cgi.getParam("do")
    val time = wiki.getLastModified(pagename)
    val login = wiki.getLoginInfo()

    // エラーチェック
    if (newpagename.isEmpty) {
      wiki.error("ページが指定されていません!!")
    }
    if (newpagename.matches("""[\|:\[\]]""")) {
      wiki.error("ページ名に使用できない文字が含まれています。")
    }
    if (wiki.pageExists(newpagename)) {
      wiki.error("既にリネーム先のページが存在します!!")
    }
    if (newpagename == pagename) {
      wiki.error("同一のページが指定されています!!")
    }
    if (!wiki.canModifyPage(pagename) || !wiki.canModifyPage(newpagename)) {
      wiki.error("ページの編集は許可されていません。")
    }
    if (wiki.pageExists(pagename)) {
      if (DateTime.parse(cgi.getParam("lastmodified")).isAfter(time.getMillis)) {
        wiki.error("ページは既に別のユーザによって更新されています。")
      }
    }

    // FrontPageを移動しようとした場合にはエラー
    if (pagename == wiki.config("frontpage") && paramDo != "copy") {
      wiki.error(wiki.config("frontpage") + "を移動することはできません。")
    }

    // コピー処理
    wiki.doHook("rename")
    val content = wiki.getPage(pagename)
    wiki.savePage(newpagename, content)

    // 削除処理
    if (paramDo == "move") {
      wiki.savePage(pagename, "")
    } else if (paramDo == "movewm") {
      wiki.savePage(pagename, "[[" + newpagename + "]]に移動しました。")
    }

    // フックの起動と返却メッセージ
    if (paramDo == "copy") {
      wiki.setTitle(pagename + "をコピーしました")
      WikiUtil.escapeHTML(pagename) + "をコピーしました。"
    } else {
      wiki.setTitle(pagename + "をリネームしました")
      WikiUtil.escapeHTML(pagename) + "をリネームしました。"
    }
  }
}
