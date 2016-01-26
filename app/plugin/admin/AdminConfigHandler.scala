///////////////////////////////////////////////////////////////////////////////
//
// FSWikiの動作設定を行うアクションハンドラ
//
///////////////////////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.admin

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import jp.gr.java_conf.hangedman.plugin._
import jp.gr.java_conf.hangedman.plugin.admin._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.util.ConfigUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import net.ceedubs.ficus.Ficus.{ booleanValueReader, stringValueReader, optionValueReader, toFicusConfig }
import net.ceedubs.ficus._
import java.io.File
import models.Accept
import models.Admin
import models.Mail
import models.Refer
import models.SiteWikiFormat
import play.Logger
import play.api.http.HeaderNames
import play.api.libs.iteratee.Enumerator
import play.api.mvc.ResponseHeader
import play.api.mvc.Result
import scala.collection.immutable.HashMap
import scala.util.{ Failure, Success, Try }

//==============================================================================
// コンストラクタ
//==============================================================================
class AdminConfigHandler(className: String, tpe: WikiPluginType, format: WikiFormat)
    extends WikiHandler(className, tpe, format) with ConfigUtil with HeaderNames {

  //===========================================================
  // installメソッド
  //===========================================================
  def install(wiki: AbstractWiki): Either[String, Boolean] = {
    Try {
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
    wiki.setTitle("環境設定")
    val bodyHtml = if (wiki.params("SAVE").nonEmpty) {
      saveConfig(wiki)
    } else {
      configForm(wiki)
    }

    Right(Result(
      header = ResponseHeader(200, Map(CONTENT_TYPE -> "text/plain")),
      body = Enumerator(bodyHtml.getBytes())
    ))
  }

  //==============================================================================
  // 設定フォーム
  //==============================================================================
  def configForm(wiki: AbstractWiki): String = {

    // 値が設定されていない場合の初期化
    val defaultValues = List(
      ("refer_level", "0"),
      ("accept_attach_delete", "0"),
      ("accept_attach_update", "1"),
      ("accept_edit", "1"),
      ("accept_show", "0")
    )

    val config = WikiUtil.loadConfigHash(wiki, wiki.config("config_file").getOrElse("./config_file")) match {
      case conf =>
        initWithDefaultValues(defaultValues)(conf)
    }

    //Wikiフォーマットの一覧を取得
    val buf: List[String] = wiki.getFormatNames

    // テンプレートにパラメータをセット
    def tmplParam(
      ACCEPT: Accept,
      ADMIN: Admin,
      AUTO_KEYWORD_PAGE: String,
      BR_MODE: String,
      DISPLAY_IMAGE: String,
      INSIDE_SAME_WINDOW: String,
      KEYWORD_SLASH_PAGE: String,
      MAIL: Mail,
      OPEN_NEW_WINDOW: String,
      PAGELIST: String,
      PART_EDIT: String,
      PART_LINK: String,
      REDIRECT: String,
      REFER: Refer,
      RSS_VERSION: String,
      SELECT: String,
      SESSION_LIMIT: String,
      SITE_TITLE: String,
      SITE_WIKI_FORMAT: List[SiteWikiFormat],
      WIKINAME: String
    ) = views.html.admin_config(
      ACCEPT,
      ADMIN,
      AUTO_KEYWORD_PAGE,
      BR_MODE,
      DISPLAY_IMAGE,
      INSIDE_SAME_WINDOW,
      KEYWORD_SLASH_PAGE,
      MAIL,
      OPEN_NEW_WINDOW,
      PAGELIST,
      PART_EDIT,
      PART_LINK,
      REDIRECT,
      REFER,
      RSS_VERSION,
      SELECT,
      SESSION_LIMIT,
      SITE_TITLE,
      SITE_WIKI_FORMAT,
      WIKINAME
    )
    /**
     * tmplParam(
     * SITE_TITLE           = config("site_title"),
     * ADMIN_NAME           = config("admin_name"),
     * ADMIN_MAIL           = config("admin_mail"),
     * ADMIN_MAIL_PUB       = config("admin_mail_pub"),
     * MAIL_PREFIX          = config("mail_prefix"),
     * MAIL_ID              = config("mail_id"),
     * MAIL_REMOTE_ADDR     = config("mail_remote_addr"),
     * MAIL_USER_AGENT      = config("mail_user_agent"),
     * MAIL_DIFF            = config("mail_diff"),
     * MAIL_BACKUP_SOURCE   = config("mail_backup_source"),
     * MAIL_MODIFIED_SOURCE = config("mail_modified_source"),
     * PAGELIST             = config("pagelist"),
     * SITE_WIKI_FORMAT     = \@site_wiki_format,
     * BR_MODE              = config("br_mode"),
     * AUTO_KEYWORD_PAGE    = config("auto_keyword_page"),
     * KEYWORD_SLASH_PAGE   = config("keyword_slash_page"),
     * WIKINAME             = config("wikiname"),
     * SESSION_LIMIT        = config("session_limit"),
     * RSS_VERSION          = config("rss_version"),
     * OPEN_NEW_WINDOW      = config("open_new_window"),
     * INSIDE_SAME_WINDOW   = config("inside_same_window"),
     * PART_EDIT            = config("partedit"),
     * PART_LINK            = config("partlink"),
     * REDIRECT             = config("redirect"),
     * "ACCEPT_EDIT_config("accept_edit")" => 1,
     * "ACCEPT_SHOW_config("accept_show")" => 1,
     * "ACCEPT_ATTACH_DELETE_config("accept_attach_delete")" => 1,
     * "ACCEPT_ATTACH_UPDATE_config("accept_attach_update")" => 1,
     * "REFER_MODE_config("refer_level")" => 1,
     * ACCEPT_USER_REGISTER => config("accept_user_register"),
     * DISPLAY_IMAGE        => config("display_image")
     * )
     *
     * "<form action=\"" + wiki.create_url() + "\" method=\"POST\">\n" +
     * tmpl.output().
     * "<input type=\"hidden\" name=\"action\" value=\"ADMINCONFIG\">\n" +
     * "</form>\n"
     */

    ""
  }

  //==============================================================================
  // 設定を保存
  //==============================================================================
  def saveConfig(wiki: AbstractWiki): String = {
    val oldConfig = WikiUtil.loadConfigHash(wiki, wiki.config("config_file").getOrElse("./config_file"))
    val newConfig = WikiUtil.loadConfigHash(wiki, wiki.config("config_file").getOrElse("./config_file"))

    val updates = List(
      ("site_title", wiki.params("site_title")),
      ("admin_name", wiki.params("admin_name")),
      ("admin_mail", wiki.params("admin_mail")),
      ("admin_mail_pub", wiki.params("admin_mail_pub")),
      ("mail_prefix", wiki.params("mail_prefix")),
      ("mail_id", wiki.params("mail_id")),
      ("mail_remote_addr", wiki.params("mail_remote_addr")),
      ("mail_user_agent", wiki.params("mail_user_agent")),
      ("mail_diff", wiki.params("mail_diff")),
      ("mail_backup_source", wiki.params("mail_backup_source")),
      ("mail_modified_source", wiki.params("mail_modified_source")),
      ("pagelist", wiki.params("pagelist")),
      ("site_wiki_format", wiki.params("site_wiki_format")),
      ("br_mode", wiki.params("br_mode")),
      ("accept_edit", wiki.params("accept_edit")),
      ("accept_show", wiki.params("accept_show")),
      ("wikiname", wiki.params("wikiname")),
      ("auto_keyword_page", wiki.params("auto_keyword_page")),
      ("keyword_slash_page", wiki.params("keyword_slash_page")),
      ("accept_attach_delete", wiki.params("accept_attach_delete")),
      ("accept_attach_update", wiki.params("accept_attach_update")),
      ("session_limit", wiki.params("session_limit")),
      ("rss_version", wiki.params("rss_version")),
      ("open_new_window", wiki.params("open_new_window")),
      ("inside_same_window", wiki.params("inside_same_window")),
      ("partedit", wiki.params("partedit")),
      ("partlink", wiki.params("partlink")),
      ("redirect", wiki.params("redirect")),
      ("refer_level", wiki.params("refer_level")),
      ("accept_user_register", wiki.params("accept_user_register")),
      ("display_image", wiki.params("display_image"))
    )

    WikiUtil.saveConfigHash(
      wiki.config("config_file").getOrElse("./config_file"),
      updateWithValues(updates)(newConfig)
    )

    /**
     * // config 情報ハッシュ内の全てのキーについて、
     * foreach val config_key (sort keys %new_config) {
     * val old = oldCconfig.{config_key}
     * val new = newConfig.{config_key}
     * // 値が更新されていたら、フック「change_config_キー名」を発行。
     * if (new != old) {
     * wiki.doHook("change_config_" . config_key, new, old)
     * }
     * }
     */

    wiki.createUrl(HashMap("action" -> "ADMINCONFIG"))
  }

  //==============================================================================
  // HTML::Templateのセレクトタグ用にリストを変換し結果を取得
  //==============================================================================
  def convertTemplateList(list: List[String], selectedValue: String): String = {
    ""
    // val ret: Array[String] = ()
    // list.foreach { value =>
    //   val selected = 0
    //   if (value == selectedValue) {
    //     selected = 1
    //   }
    //   //push(@ret,{VALUE=>value,SELECT=>selected})
    // }
    //@ret
  }

}
