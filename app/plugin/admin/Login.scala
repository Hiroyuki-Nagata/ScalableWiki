///////////////////////////////////////////////////////////////////////////////
//
// 管理者ログイン
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
import play.api.mvc.Cookie
import play.api.mvc.DiscardingCookie
import play.api.mvc.ResponseHeader
import play.api.mvc.Result
import scala.collection.immutable.HashMap
import scala.util.{ Failure, Success, Try }

//==============================================================================
// コンストラクタ
//==============================================================================
class Login(className: String, tpe: WikiPluginType, format: WikiFormat)
    extends WikiHandler(className, tpe, format) with HeaderNames {

  //===========================================================
  // installメソッド
  //===========================================================
  def install(wiki: AbstractWiki): Either[String, Boolean] = {
    Try {
      // Install.install(wiki)
    } match {
      case Success(_) =>
        Right(true)
      case Failure(e) =>
        Logger.error(e.getMessage, e)
        Left(e.getMessage)
    }
  }

  //==============================================================================
  // アクションハンドラ
  //==============================================================================
  def doAction(wiki: AbstractWiki): Either[String, play.api.mvc.Result] = {

    wiki.setTitle("管理")
    if (wiki.params("logout").nonEmpty) {
      logout(wiki)
    }

    if (wiki.getLoginInfo.isDefined) {
      adminForm(wiki, wiki.getLoginInfo.get)
    } else {
      // ログインの判定
      val id = wiki.params("id")
      val pass = wiki.params("pass")
      val page = wiki.params("page")

      if (id.nonEmpty && pass.nonEmpty) {
        wiki.checkLogin(id, WikiUtil.md5(pass, id)) match {
          case Some(login: LoginInfo) =>
            val session = wiki.getSession
            session + (("wiki_id", id))
            session + (("wiki_type", login.tpe.toString))
            session + (("wiki_path", login.path))
            //session.flush()
            if (page.nonEmpty) {
              wiki.redirectURL(wiki.createPageUrl(page))
            } else {
              wiki.redirectURL(wiki.createUrl(HashMap("action" -> "LOGIN")))
            }
          case None =>
            wiki.errorL("IDもしくはパスワードが違います。")
        }
      }
    }

    val bodyHtml = default(wiki)

    Right(Result(
      header = ResponseHeader(200, Map(CONTENT_TYPE -> "text/plain")),
      body = Enumerator(bodyHtml.getBytes())
    ))
  }

  //==============================================================================
  // 管理画面フォーム
  //==============================================================================
  def adminForm(wiki: AbstractWiki, login: LoginInfo): String = {

    val buf = new StringBuilder("<h2>ログイン中</h2>\n")

    // 管理者ユーザの場合
    if (login.tpe == Administrator) {
      buf.append("<ul>\n")
      wiki.getAdminMenu.foreach { menu =>
        buf.append("<li><a href=\"" + menu.url + "\">" + menu.label + "</a>")
        buf.append(" - " + WikiUtil.escapeHTML(menu.desc))
        buf.append("</li>\n")
      }
      buf.append("</ul>\n")
    } else { // 一般ユーザの場合
      buf.append("<ul>\n")
      wiki.getAdminMenu.foreach { menu =>
        if (login.tpe == Administrator) {
          buf.append("<li><a href=\"" + menu.url + "\">" + menu.label + "</a>")
          buf.append(" - " + WikiUtil.escapeHTML(menu.desc))
          buf.append("</li>\n")
        }
      }
      buf.append("</ul>\n")
    }

    buf.append("<form action=\"" + wiki.createUrl + "\" method=\"POST\">" +
      "  <input type=\"submit\" name=\"logout\" value=\"ログアウト\">" +
      "  <input type=\"hidden\" name=\"action\" value=\"LOGIN\">" +
      "</form>\n")

    buf.result
  }

  //==============================================================================
  // ログアウト処理
  //==============================================================================
  def logout(wiki: AbstractWiki): play.api.mvc.Result = {

    // Cookieの破棄
    val path = WikiUtil.cookiePath(wiki)

    wiki.redirectURL(
      wiki.createUrl(HashMap("action" -> "LOGIN"))
    ).discardingCookies(DiscardingCookie("CGISESSID"))
      .withCookies(Cookie("CGISESSID", "", None, path, None))
      .withNewSession // Sessionの破棄
  }

  //==============================================================================
  // ログイン画面
  //==============================================================================
  def default(wiki: AbstractWiki): String = {

    def tmplParam(
      URL: String,
      PAGE: String,
      ACCEPT_USER_REGISTER: String
    ) = views.html.login(
      URL: String,
      PAGE: String,
      ACCEPT_USER_REGISTER: String
    )

    tmplParam(
      wiki.createUrl(),
      wiki.params("page"),
      wiki.config("accept_user_register").get
    ).body
  }
}
