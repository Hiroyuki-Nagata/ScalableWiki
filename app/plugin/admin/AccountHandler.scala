///////////////////////////////////////////////////////////////////////////////
//
// アカウント情報管理を行うアクションハンドラ
//
///////////////////////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.admin

import jp.gr.java_conf.hangedman.plugin._
import jp.gr.java_conf.hangedman.plugin.admin._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import play.api.http.HeaderNames
import play.api.libs.iteratee.Enumerator
import play.api.mvc.ResponseHeader
import play.api.mvc.Result
import scala.util.{ Failure, Success, Try }

//==============================================================================
// コンストラクタ
//==============================================================================
class AccountHandler(className: String, tpe: WikiPluginType, format: WikiFormat)
    extends WikiHandler(className, tpe, format) with HeaderNames {

  //===========================================================
  // installメソッド
  //===========================================================
  def install(wiki: AbstractWiki): Either[String, Boolean] = {
    Try {
      // TODO: Implement it
      //Install.install(wiki)
    } match {
      case Success(_) =>
        Right(true)
      case Failure(e) =>
        Logger.error(e.getMessage, e)
        Left(e.getMessage)
    }
  }

  //==============================================================================
  // アクションハンドラメソッド
  //==============================================================================
  def doAction(wiki: AbstractWiki): Either[String, play.api.mvc.Result] = {

    wiki.setTitle("アカウント情報")
    if (wiki.params("changepass").nonEmpty) {
      this.changePass(wiki)
    }
    if (!wiki.getLoginInfo.isDefined) {
      wiki.errorL("ログインしていません。")
    }
    val id = wiki.getLoginInfo.get("id")
    val bodyHtml = accountForm(wiki, id)

    Right(Result(
      header = ResponseHeader(200, Map(CONTENT_TYPE -> "text/plain")),
      body = Enumerator(bodyHtml.getBytes())
    ))
  }

  //==============================================================================
  // アカウント情報フォーム
  //==============================================================================
  def accountForm(wiki: AbstractWiki, id: String): String = {

    val buf = new StringBuilder("<h2>アカウント情報</h2>")
    buf.append("<form action=\"" + wiki.createUrl + "\" method=\"POST\">\n")
    buf.append("  <table>\n")
    buf.append("  <tr>\n")
    buf.append("  <th>ID</th>\n")
    buf.append("  <td><b>" + WikiUtil.escapeHTML(id) + "</b>（変更はできません）</td>\n")
    buf.append("  </tr>\n")
    buf.append("  <tr>\n")
    buf.append("  <th>現在のパスワード</th>\n")
    buf.append("  <td><input type=\"password\" name=\"pass_old\" size=\"30\"></td>\n")
    buf.append("  </tr>\n")
    buf.append("  <tr>\n")
    buf.append("  <th>新しいパスワード</th>\n")
    buf.append("  <td><input type=\"password\" name=\"pass1\" size=\"30\"></td>\n")
    buf.append("  </tr>\n")
    buf.append("  <tr>\n")
    buf.append("  <th>新しいパスワード（確認）</th>\n")
    buf.append("  <td><input type=\"password\" name=\"pass2\" size=\"30\"></td>\n")
    buf.append("  </tr>\n")
    buf.append("  </table>\n")
    //	$buf .= "  <div style=\"margin-top:10pt;\">\n";
    buf.append("    <input type=\"submit\" name=\"changepass\" value=\"変更\">\n")
    buf.append("    <input type=\"hidden\" name=\"action\" value=\"ACCOUNT\">\n")
    buf.append("    <input type=\"hidden\" name=\"id\" value=\"" + WikiUtil.escapeHTML(id) + "\">\n")
    //	$buf .= "  </div>\n";
    buf.append("</form>\n")

    buf.result
  }

  //==============================================================================
  // パスワードの変更
  //==============================================================================
  def changePass(wiki: AbstractWiki): String = {

    val id = wiki.params("id")
    val passOld = wiki.params("pass_old")
    val pass = wiki.params("pass1")
    val passConfirm = wiki.params("pass2")

    // 現在のパスワードの照合
    // 他人がパスワードを変更してしまうことを防止するため、パスワードを変更
    // する際には現在のパスワードを照合する必要がある。
    //wiki.loginCheck(id, WikiUtil.md5(passOld, id)) match {
    wiki.getLoginInfo() match {
      case Some(login) =>
        val minLength = 2

        // 新しいパスワードの正当性の確認
        if (pass.length < minLength) {
          wiki.error("新しいパスワードが入力されていません。" +
            "少なくとも min_length 文字以上入力してください。")
        } else if (pass != passConfirm) {
          wiki.error("入力された二つのパスワードが合致しません。")
        }

        // val session = cgi.get_session(wiki)
        // session.param("wiki_id", id)
        // session.param("wiki_type", login("type"))
        // session.param("wiki_path", login("path"))
        // session.flush()

        val users = WikiUtil.loadConfigHash(wiki, wiki.config("userdat_file").get)
        //my (p,type)  = split(/\t/,users.{id})
        //users("id") = WikiUtil.md5(pass, id) + "\ttype"
        WikiUtil.saveConfigHash(wiki.config("userdat_file").get, users)
        ""
      case None =>
        wiki.error("現在のパスワードが違います。")
    }

    //wiki.redirectUrl(wiki.createUrl(HashMap("action" -> "LOGIN")))

    //"<p>パスワードを変更しました。</p>" +
    //       "[<a href=\"" + wiki.config("script_name") + "?action=LOGIN\">メニューに戻る</a>]\n"
  }

}
