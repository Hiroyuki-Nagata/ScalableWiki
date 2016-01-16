package jp.gr.java_conf.hangedman.plugin.amazon

import jp.gr.java_conf.hangedman.plugin.amazon._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.util.{ Failure, Success, Try }

///////////////////////////////////////////////////////////////////////////////
//
// <p>指定した書籍の書影をamazonから取得して表示し、amazonの書評ページへリンクをはります。</p>
// <pre>
//   {{amazon asin[,comment]}}
// </pre>
// <p>
//   setup.dat に amazon_aid という定数を設定すると amazon のアソシエトID つきでリンクがはられます。
// </p>
// <p>
//   イメージが存在しないかどうか確認するためにamazonのサーバに接続しているので、
//   プロキシ経由で外に出る必要がある場合は、プロキシの設定情報をsetup.datに設定しておく必要があります。
// </p>
// <p>
//   comment 引数があたえられると、書影画像のかわりにその文字列からリンクをはります。
// </p>
//
///////////////////////////////////////////////////////////////////////////////

//==============================================================================
// コンストラクタ
//==============================================================================
class Amazon(className: String, tpe: WikiPluginType, format: WikiFormat)
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

  def hook(wiki: AbstractWiki, name: String, args: String*) = { "" }

  //==============================================================================
  // インラインメソッド
  //==============================================================================
  def inline(wiki: AbstractWiki, rawItem: String, comment: String): String = {

    val item: String = WikiUtil.escapeHTML(rawItem)
    val aid: Option[String] = wiki.config("amazon_aid")

    val link: String = (WikiUtil.handyphone()(wiki.request), aid) match {
      case (true, Some(aid)) =>
        "http://www.amazon.co.jp/gp/aw/rd.html?uid=NULLGWDOCOMOat=" + aid + "a=" + item + "dl=1url=%2Fgp%2Faw%2Fd.html"
      case (true, None) =>
        "http://www.amazon.co.jp/gp/aw/rd.html?uid=NULLGWDOCOMOa=" + item + "dl=1url=%2Fgp%2Faw%2Fd.html"
      case (false, None) =>
        "http://www.amazon.co.jp/exec/obidos/ASIN/" + item
      case (false, Some(aid)) =>
        "http://www.amazon.co.jp/exec/obidos/ASIN/" + item + "/" + aid + "/ref=nosim"
    }

    def testAmazonImage(num: String): String = {
      WikiUtil.getResponse(wiki, "http://images-jp.amazon.com/images/P/${item}.${num}.MZZZZZZZ.jpg")
    }

    val buf: String = if (comment.isEmpty) {
      val noimg: String = "http://images-jp.amazon.com/images/G/09/icons/books/comingsoon_books.gif"
      val image: String = testAmazonImage("09") match {
        case response if (response.length > 1024) =>
          "http://images-jp.amazon.com/images/P/${item}.09.MZZZZZZZ.jpg"
        case _ =>
          testAmazonImage("01") match {
            case response if (response.length > 1024) =>
              "http://images-jp.amazon.com/images/P/${item}.01.MZZZZZZZ.jpg"
            case _ =>
              noimg
          }
      }
      "<img src=\"${image}\">"
    } else {
      WikiUtil.escapeHTML(comment)
    }

    "<span class=\"amazonb\"><a href=\"${link}\">${buf}\"</a></span>"
  }
}
