package jp.gr.java_conf.hangedman.controllers

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigValue
import java.io.File
import jp.gr.java_conf.hangedman.model.WikiPlugin
import jp.gr.java_conf.hangedman.util.wiki.Wiki
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model.PathInfo
import jp.gr.java_conf.hangedman.model.{ Users, User }
import net.ceedubs.ficus._
import net.ceedubs.ficus.Ficus.{ booleanValueReader, stringValueReader, optionValueReader, toFicusConfig }
import play.api._
import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import collection.JavaConversions._
import play.twirl.api.Html
import scala.collection.immutable.HashMap
import scala.io.Source

object Application extends Controller {

  def overwriteConfigs(configMaps: Map[String, List[String]])(implicit wiki: Wiki) = {
    configMaps.foreach { m =>
      overwriteConfig(m._1, m._2)
    }
  }

  def overwriteConfig(key: String, params: List[String])(implicit wiki: Wiki) = {
    // take last of params
    // get config values and concat values
    val path: String = params.init.map {
      e: String => if (!e.contains("/")) wiki.config(e)
    }.mkString + params.last
    wiki.config(key, path)
  }

  def params(key: String)(implicit request: play.api.mvc.Request[AnyContent]): String = {
    request.queryString.get(key) match {
      case Some(value) if (value.size == 1) =>
        value.head
      case Some(value) if (value.size != 1) =>
        value.mkString(",")
      case None =>
        ""
    }
  }

  def index = Action { implicit request =>

    // make instance of Wiki and CGI
    val wiki = new Wiki("setup.conf", request)

    // use directory for session, use also for Farm
    wiki.config("session_dir", wiki.config("log_dir").getOrElse("./log"))

    /*
     * In a case, Swiki works as Farm
    if (cgi.pathInfo.length > 0) {
      // blah, blah, blah
    }
     */

    ////////////////////////////////////////////////////////////////////////////////
    // reflect configures ( I would like to do it more smart )
    ////////////////////////////////////////////////////////////////////////////////
    val configs = WikiUtil.loadConfigHash(wiki, wiki.config("config_file").getOrElse("./config_file"))
    configs.foreach {
      case (key, value) =>
        Logger.debug(s"configs ${key} => ${value}")
        wiki.config(key, value)
    }

    // overwrite configs if it needs
    overwriteConfigs(
      Map(
        "css" ->
          List("theme_uri", "/", configs("theme"), "/", configs("theme"), ".css"),
        "site_tmpl" ->
          List("tmpl_dir", "/site/", configs("site_tmpl_theme"), "/", configs("site_tmpl_theme"), ".tmpl"),
        "site_handyphone_tmpl" ->
          List("tmpl_dir", "/site/", configs("site_tmpl_theme"), "/", configs("site_tmpl_theme"), "_handyphone.tmpl"),
        "site_smartphone_tmpl" ->
          List("tmpl_dir", "/site/", configs("site_tmpl_theme"), "/", configs("site_tmpl_theme"), "_smartphone.tmpl")
      )
    )(wiki)

    // destroy timeouted session
    //cgi.removeSession(wiki)

    // load user information
    Cache.getAs[Users]("wiki.users") match {
      case Some(users: Users) =>
        users.users.foreach { user =>
          Logger.debug(s"Adding cached user => ${user}")
          wiki.addUser(user)
        }
      case None =>
      //
    }

    // install and initialize plugins
    Source.fromFile("conf/" + wiki.config("plugin_file").getOrElse("plugin.dat")).getLines.foreach {
      line => wiki.installPlugin(line)
    }

    // start plugins each initialization
    wiki.doHook("initialize")

    // call action handler
    //val action = cgi.paramAction.get
    //val content = wiki.callHandler(action)

    // FIXME: +error handling

    // Response
    val isHandyPhone: Boolean = WikiUtil.handyphone
    val isSmartPhone: Boolean = WikiUtil.smartphone

    // process site template
    def siteTemplate(
      EDIT_MODE: String,
      CAN_SHOW: String,
      HEAD_INFO: Html,
      THEME_CSS: String,
      HAVE_USER_CSS: String,
      USER_CSS: String,
      SITE_TITLE: String,
      MENU: String,
      TITLE: String,
      EXIST_PAGE_Menu: String,
      EXIST_PAGE_Header: String,
      CONTENT: String,
      EXIST_PAGE_Footer: String,
      FOOTER: Html
    ) = (isHandyPhone, isSmartPhone) match {
      case (true, false) =>
        views.html.site.default.default_handyphone(
          SITE_TITLE,
          MENU,
          TITLE,
          CONTENT,
          FOOTER
        )
      case (false, true) =>
        views.html.site.default.default_smartphone(
          EDIT_MODE,
          CAN_SHOW,
          HEAD_INFO,
          THEME_CSS,
          HAVE_USER_CSS,
          USER_CSS,
          SITE_TITLE,
          MENU,
          TITLE,
          EXIST_PAGE_Header,
          CONTENT,
          EXIST_PAGE_Footer,
          FOOTER
        )
      case (_, _) =>
        views.html.site.default.default(
          EDIT_MODE,
          CAN_SHOW,
          HEAD_INFO,
          THEME_CSS,
          HAVE_USER_CSS,
          USER_CSS,
          SITE_TITLE,
          MENU,
          TITLE,
          EXIST_PAGE_Menu,
          EXIST_PAGE_Header,
          CONTENT,
          EXIST_PAGE_Footer,
          FOOTER
        )
    }

    // detect this page is top or not
    val top = if (params("page") == wiki.config("frontpage")) {
      1
    } else {
      0
    }

    // determine page title
    val title = if (params("action").isEmpty &&
      wiki.pageExists(params("page")) &&
      wiki.isInstalled("search")) {
      val href = wiki.createUrl(HashMap("SEARCH" -> wiki.getTitle))
      val escapedTitle = WikiUtil.escapeHTML(wiki.getTitle)
      "<a href=\"%s\">%s</a>".format(href, escapedTitle)
    } else {
      WikiUtil.escapeHTML(wiki.getTitle)
    }

    //
    // generate header
    //
    val headerTmpl = views.html.header(List(models.Menu("http://www.google.com", "test")), "")
    // FIXME: Get Menu

    //
    // generate footer
    //
    val footerTmpl = views.html.footer(
      (wiki.config("admin_mail_pub"), wiki.config("admin_name")) match {
        case (Some(mail), Some(name)) =>
          "true" // OUT_COPYRIGHT: true
        case (_, _) =>
          "" // false
      },
      wiki.config("admin_mail_pub").getOrElse("admin@mail.com"),
      wiki.config("admin_name").getOrElse("admin"),
      "0.0.1-SNAPSHOT", // this module version
      "Scala", // lang name
      scala.util.Properties.versionString, // scala version
      "" // play version
    )
    // FIXME: Get Menu

    //String content = Page.getContentOf(page);
    //response().setContentType("text/html");

    val contentType = if (isHandyPhone) {
      "text/html;charset=Shift_JIS"
    } else {
      "text/html;charset=UTF-8"
    }

    // Set parameters in template
    val wikiHtml = siteTemplate(
      "true", // EDIT_MODE
      "true", // CAN_SHOW
      headerTmpl, // HEAD_INFO
      "css", // THEME_CSS
      "true", // HAVE_USER_CSS
      "css", // USER_CSS
      // SITE_TITLE
      wiki.getTitle + " - " + wiki.config("site_title").getOrElse("[ScalableWiki]"),
      "Menu", // MENU
      "Default Title", // TITLE
      "Page Menu", // EXIST_PAGE_Menu
      "Page Header", // EXIST_PAGE_Header
      "Contents", // CONTENT
      "Page Footer", // EXIST_PAGE_Footer
      footerTmpl // FOOTER
    )

    // Output HTML
    Result(
      header = ResponseHeader(
        200,
        Map(
          CONTENT_TYPE -> contentType,
          PRAGMA -> "no-cache",
          CACHE_CONTROL -> "no-cache"
        )
      ),
      body = Enumerator(
        wikiHtml.toString.getBytes
      )
    )
  }
}
