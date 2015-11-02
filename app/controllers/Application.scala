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
import net.ceedubs.ficus._
import net.ceedubs.ficus.Ficus.{ booleanValueReader, stringValueReader, optionValueReader, toFicusConfig }
import play.api._
import play.api.mvc._
import collection.JavaConversions._
import scala.io.Source

object Application extends Controller {

  // make instance of Wiki and CGI
  val wiki = new Wiki("setup.conf")
  val cgi = wiki.getCGI

  def overwriteConfigs(configMaps: Map[String, List[String]]) = {
    configMaps.foreach { m =>
      overwriteConfig(m._1, m._2)
    }
  }

  def overwriteConfig(key: String, params: List[String]) = {
    // take last of params
    // get config values and concat values
    val path: String = params.init.map {
      e: String => if (!e.contains("/")) wiki.config(e)
    }.mkString + params.last
    wiki.config(key, path)
  }

  def index = Action {

    // use directory for session, use also for Farm
    wiki.config("session_dir", wiki.config("log_dir").getOrElse("./log"))

    /*
     * In a case, Swiki works as Farm 
     */
    if (cgi.pathInfo.length > 0) {
      // blah, blah, blah
    }

    // overwrite configs if it needs
    overwriteConfigs(
      Map(
        "css" ->
          List("theme_uri", "/", "theme", "/", "theme", ".css"),
        "site_tmpl" ->
          List("tmpl_dir", "/site/", "site_tmpl_theme", "/", "site_tmpl_theme", ".tmpl"),
        "site_handyphone_tmpl" ->
          List("tmpl_dir", "/site/", "site_tmpl_theme", "/", "site_tmpl_theme", "_handyphone.tmpl"),
        "site_smartphone_tmpl" ->
          List("tmpl_dir", "/site/", "site_tmpl_theme", "/", "site_tmpl_theme", "_smartphone.tmpl")
      )
    )

    // destroy timeouted session
    cgi.removeSession(wiki)

    // load user information
    ConfigFactory.parseFile(new File("conf/" + wiki.config("userdat_file").getOrElse("user.properties"))) match {
      case config: Config =>
        config.root.foreach {
          case (id: String, config: ConfigValue) =>
            println(s"Adding ${id} as wiki user...")

            Option(config.render) match {
              case Some(value) =>
                if (value.split('\t').length == 2)
                  wiki.addUser(id, value.split('\t')(0), value.split('\t')(1).toInt)
                println(s"Add user ${id}...")
              case None =>
                println(s"User ${id} is invalid")
            }
          case _ =>
            println("Other elements...")
        }
      case _ =>
        println("userdat_file not found")
    }

    // install and initialize plugins
    Source.fromFile("conf/" + wiki.config("plugin_file").getOrElse("plugin.dat")).getLines.foreach {
      line => wiki.installPlugin(line)
    }

    // start plugins each initialization
    wiki.doHook("initialize")

    // call action handler
    val action = cgi.paramAction.get
    val content = wiki.callHandler(action)

    // FIXME: +error handling

    // Response
    val isHandyPhone = WikiUtil.handyphone
    val isSmartPhone = WikiUtil.smartphone

    val templateName = (isHandyPhone, isSmartPhone) match {
      case (true, false) =>
        "site_handyphone_tmpl"
      case (false, true) =>
        "site_smartphone_tmpl"
      case (_, _) =>
        "site_tmpl"
    }

    // detect this page is top or not
    val top = if (cgi.paramPage == wiki.config("frontpage")) {
      1
    } else {
      0
    }

    // determine page title
    val title = if (cgi.paramAction.isEmpty && wiki.pageExists(cgi.paramPage) && wiki.isInstalled("search")) {

      val href = wiki.createUrl("SEARCH", wiki.getTitle)
      val escapedTitle = WikiUtil.escapeHTML(wiki.getTitle)
      "<a href=\"%s\">%s</a>".format(href, escapedTitle)
    } else {
      WikiUtil.escapeHTML(wiki.getTitle)
    }

    //
    // generate header
    //

    Ok(views.html.index("Your new application is ready."))
  }

}
