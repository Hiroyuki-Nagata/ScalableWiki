///////////////////////////////////////////////////////////////////////////////
//
// MIMEタイプの設定を行うアクションハンドラ
//
///////////////////////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.attach

import jp.gr.java_conf.hangedman.plugin.attach._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.collection.immutable.HashMap
import scala.util.{ Failure, Success, Try }

//==============================================================================
// コンストラクタ
//==============================================================================
class AdminMIMEHandler(className: String, tpe: WikiPluginType, format: WikiFormat)
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
  // アクションハンドラメソッド
  //==============================================================================
  def doAction(wiki: AbstractWiki): Either[String, play.api.mvc.Result] = {
    val cgi = wiki.getCGI
    wiki.setTitle("MIMEタイプの設定")

    if (cgi.getParam("ADD").nonEmpty) {
      this.add(wiki)
    } else if (cgi.getParam("DELETE").nonEmpty) {
      this.delete(wiki)
    } else {
      Left(this.form(wiki))
    }
  }

  //==============================================================================
  // 一覧画面
  //==============================================================================
  def form(wiki: AbstractWiki): String = {
    val buf = new StringBuilder("<h2>MIMEタイプの登録</h2>\n" +
      "<form action=\"" + wiki.createUrl + "\" method=\"POST\">\n" +
      "  拡張子（ドットは不要） <input type=\"text\" name=\"extention\" size=\"5\">\n" +
      "  MIMEタイプ <input type=\"text\" name=\"mimetype\" size=\"20\">\n" +
      "  <input type=\"submit\" name=\"ADD\" value=\"登録\">\n" +
      "  <input type=\"hidden\" name=\"action\" value=\"ADMINMIME\">\n" +
      "</form>\n" +
      "<h2>登録済のMIMEタイプ</h2>\n" +
      "<form action=\"" + wiki.createUrl + "\" method=\"POST\">\n" +
      "<table>\n" +
      "<tr><th>nbsp</td><th>拡張子</th><th>MIMEタイプ</th></tr>\n")

    val mime = WikiUtil.loadConfigHash(wiki.config("mime_file").getOrElse("./mime_file"))

    // foreach val key (sort(keys(%mime))){
    //   buf += "<tr>\n" +
    //   "  <td><input type=\"checkbox\" name=\"extention\" value=\"" + WikiUtil.escapehtml(key) + "\"></td>\n" +
    //   "  <td>" + WikiUtil.escapehtml(key) + "</td>\n" +
    //   "  <td>" + WikiUtil.escapehtml(mime.{key}) + "</td>\n" +
    //   "</tr>\n"
    // }
    buf.append("</table>\n" +
      "<input type=\"submit\" name=\"DELETE\" value=\"選択項目を削除\">\n" +
      "<input type=\"hidden\" name=\"action\" value=\"ADMINMIME\">\n" +
      "</form>\n")

    buf.result
  }

  //==============================================================================
  // 追加
  //==============================================================================
  def add(wiki: AbstractWiki): Either[String, play.api.mvc.Result] = {
    val cgi = wiki.getCGI()

    val ext = cgi.getParam("extention")
    val mime = cgi.getParam("mimetype")

    if (ext.nonEmpty && mime.nonEmpty) {
      val hash = WikiUtil.loadConfigHash(wiki.config("mime_file").getOrElse("./mime_file"))
      WikiUtil.saveConfigHash(
        wiki.config("mime_file").getOrElse("./mime_file"),
        hash.updated("ext", mime)
      )
      Right(wiki.redirectURL(wiki.createUrl(HashMap("action" -> "ADMINMIME"))))
    } else {
      Left(wiki.error("拡張子とMIMEタイプを入力してください。"))
    }
  }

  //==============================================================================
  // 削除
  //==============================================================================
  def delete(wiki: AbstractWiki): Either[String, play.api.mvc.Result] = {
    val cgi = wiki.getCGI

    //val ext_list: Array[String] = cgi.getParam("extention")
    val hash = WikiUtil.loadConfigHash(wiki.config("mime_file").getOrElse("./mime_file"))
    val result = hash

    // foreach val key (keys(%hash)){
    //   val flag = 0
    //   ext_list.foreach { ext  =>
    //     if(ext == key){
    //       flag = 1
    //       last
    //     }
    //   }
    //   if(flag==0){
    //     result.("key") = hash.{key}
    //   }
    // }

    WikiUtil.saveConfigHash(wiki.config("mime_file").getOrElse("./mime_file"), result)
    Right(wiki.redirectURL(wiki.createUrl(HashMap { "action" -> "ADMINMIME" })))
  }
}
