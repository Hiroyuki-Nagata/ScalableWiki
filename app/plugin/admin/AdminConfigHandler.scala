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
      ("accept_show", "0"),
      ("accept_user_register", "1"),
      ("inside_same_window", "1"),
      ("partlink", "0"),
      ("redirect", "0"),
      ("select", "1")
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

    val accept = Accept(
      config("accept_attach_delete"),
      config("accept_attach_delete"),
      config("accept_attach_delete"),
      config("accept_attach_update"),
      config("accept_attach_update"),
      config("accept_attach_update"),
      config("accept_edit"),
      config("accept_edit"),
      config("accept_edit"),
      config("accept_show"),
      config("accept_show"),
      config("accept_show"),
      config("accept_user_register")
    )

    val admin = Admin(
      config("admin_mail"),
      config("admin_mail_pub"),
      config("admin_name")
    )

    val mail = Mail(
      config("mail_remote_addr"),
      config("mail_backup_source"),
      config("mail_prefix"),
      config("mail_diff"),
      config("mail_user_agent"),
      config("mail_id"),
      config("mail_modified_source")
    )

    val refer = Refer(
      config("refer_level"),
      config("refer_level"),
      config("refer_level")
    )

    // テンプレートの出力結果を返す
    "<form action=\"" + wiki.createUrl + "\" method=\"POST\">\n" +
      tmplParam(
        accept, // ACCEPT: Accept,
        admin, // ADMIN: Admin,
        config("auto_keyword_page"), // AUTO_KEYWORD_PAGE: String,
        config("br_mode"), // BR_MODE: String,
        config("display_image"), // DISPLAY_IMAGE: String,
        config("inside_same_window"), // INSIDE_SAME_WINDOW: String,
        config("keyword_slash_page"), // KEYWORD_SLASH_PAGE: String,
        mail, // MAIL: Mail,
        config("open_new_window"), // OPEN_NEW_WINDOW: String,
        config("pagelist"), // PAGELIST: String,
        config("partedit"), // PART_EDIT: String,
        config("partlink"), // PART_LINK: String,
        config("redirect"), // REDIRECT: String,
        refer, // REFER: Refer,
        config("rss_version"), // RSS_VERSION: String,
        config("select"), // SELECT: String,
        config("session_limit"), // SESSION_LIMIT: String,
        config("site_title"), // SITE_TITLE: String,
        Nil, // SITE_WIKI_FORMAT: List[SiteWikiFormat],
        config("wikiname") // WIKINAME: String
      ).body +
        "<input type=\"hidden\" name=\"action\" value=\"ADMINCONFIG\">\n" +
        "</form>\n"
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

    // config 情報ハッシュ内の全てのキーについて
    newConfig.foreach {
      case (key, newValue) =>
        val oldValue = oldConfig(key)
        if (newValue != oldValue) {
          // 値が更新されていたら、フック「change_config_キー名」を発行。
          wiki.doHook(s"change_config_${key}", newValue, oldValue)
        }
    }

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
