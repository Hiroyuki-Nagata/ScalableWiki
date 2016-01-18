package jp.gr.java_conf.hangedman.util

import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import javax.mail.internet.MimeUtility
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import org.joda.time.DateTime
import play.Logger
import play.api.mvc.AnyContent
import play.api.mvc.Request
import scala.collection.immutable.HashMap
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object WikiUtil {

  def overrideDie() = {}

  /**
   * 引数で渡された文字列をURLエンコードして返します。
   * {{{
   * val str = WikiUtil.urlEncode(str)
   * }}}
   */
  def urlEncode(rawString: String, enc: String = "utf-8"): String = {
    Try {
      URLEncoder.encode(rawString, enc)
    } match {
      case Success(encoded) =>
        encoded
      case Failure(e) =>
        Logger.error(s"Fail to URL encode ${rawString} =>", e)
        ""
    }
  }
  /**
   * 引数で渡された文字列をURLデコードして返します。
   * {{{
   * val str = WikiUtil.urlDecode(str)
   * }}}
   */
  def urlDecode(encString: String, enc: String = "utf-8"): String = {
    Try {
      URLDecoder.decode(encString, enc)
    } match {
      case Success(decoded) =>
        decoded
      case Failure(e) =>
        Logger.error(s"Fail to URL decode ${encString} =>", e)
        ""
    }
  }
  /**
   * Cookieのpathに指定する文字列を取得します。
   * {{{
   * val path = WikiUtil.cookiePath(wiki)
   * }}}
   */
  def cookiePath(wiki: AbstractWiki): String = {
    ""
  }
  /**
   * ディレクトリ、ファイル名、拡張子を結合してファイル名を生成します。
   * {{{
   * val filename = WikiUtil.makeFilename(ディレクトリ名,ファイル名,拡張子)
   * }}}
   */
  def makeFilename(dir: String, file: String, ext: String): String = {
    s"${dir}/${file}/.${ext}"
  }
  /**
   * 引数で渡された文字列のHTMLタグをエスケープして返します。
   * {{{
   * str = WikiUtil.escapeHTML(str)
   * }}}
   */
  def escapeHTML(html: String): String = scala.xml.Utility.escape(html)
  /**
   * 日付を&quot;yyyy年mm月dd日 hh時mi分ss秒&quot;形式にフォーマットします。
   * {{{
   * val date = WikiUtil.formatDate(time())
   * }}}
   */
  def formatDate(dt: DateTime): String = {
    dt.toString("yyyy年MM月dd日 HH時mm分ss秒")
  }
  /**
   * 文字列の両端の空白を切り落とします。
   * {{{
   * val text = WikiUtil.trim(text)
   * }}}
   * @deprecated
   */
  def trim(text: String): String = text.trim
  /**
   * タグを削除して文字列のみを取得します。
   * {{{
   * val html: String = "<B>文字列</B>"
   * // &lt;B&gt;と&lt;/B&gt;を削除し、&quot;文字列&quot;のみ取得
   * val text = WikiUtil.deleteTag(html)
   * }}}
   */
  def deleteTag(text: String): String = {
    """<(.|\s)+?>""".r
      .replaceAllIn(text, m => m.group(1))
  }
  /**
   * 数値かどうかチェックします。数値の場合は真、そうでない場合は偽を返します。
   * {{{
   * if (WikiUtil.checkNumeric(param)) {
   *   // 整数の場合の処理
   * } else {
   *   // 整数でない場合の処理
   * }
   * }}}
   */
  def checkNumeric(text: String) = {
    text.matches("""^[0-9]+$""")
  }
  /**
   * 管理者にメールを送信します。
   * setup.datの設定内容に応じてsendmailコマンドもしくはSMTP通信によってメールが送信されます。
   * どちらも設定されていない場合は送信を行わず、エラーにもなりません。
   * SMTPで送信する場合、このメソッドを呼び出した時点でNet::SMTPがuseされます。
   *
   * {{{
   * WikiUtil.sendMail(wiki,件名,本文)
   * }}}
   */
  def sendMail(wiki: AbstractWiki, rawSubject: String, rawContent: String) = {
    val subject = Try {
      MimeUtility.encodeText(rawSubject)
    } match {
      case Success(mime) =>
        mime
      case Failure(e) =>
        ""
    }
  }
  /**
   * クライアントが携帯電話かどうかチェックします。
   * 携帯電話の場合は真、そうでない場合は偽を返します。
   * {{{
   * if (WikiUtil.handyphone) {
   *   // 携帯電話の場合の処理
   * } else {
   *   // 携帯電話でない場合の処理
   * }
   * }}}
   */
  def handyphone()(implicit request: Request[AnyContent]): Boolean = {
    val userAgent = getUserAgent
    if (userAgent.matches("""^DoCoMo\/.*$|^J-PHONE\/.*$|UP\.Browser.*$|\(DDIPOCKET\;.*$|\(WILLCOM\;.*$|^Vodafone\/.*$|^SoftBank\/.*$""")) {
      true
    } else {
      false
    }
  }
  /**
   * クライアントがスマートフォンかどうかチェックします。
   * スマートフォンの場合は真、そうでない場合は偽を返します。
   * {{{
   * if (WikiUtil.smartphone) {
   *   // スマートフォンの場合の処理
   * } else {
   *   // スマートフォンでない場合の処理
   * }
   * }}}
   */
  def smartphone()(implicit request: Request[AnyContent]): Boolean = {
    val userAgent = getUserAgent
    if (userAgent.matches(""".*Android.*|.*iPhone.*""")) {
      true
    } else {
      false
    }
  }

  private def getUserAgent()(implicit request: Request[AnyContent]): String = {
    Try {
      request.headers("User-Agent")
    } match {
      case Success(ua) =>
        ua
      case Failure(e) =>
        ""
    }
  }
  /**
   * loadConfigHash関数で使用するアンエスケープ用関数
   */
  private val unescape = (value: String) => {
    val table = HashMap("\\\\" -> "\\", "\\n" -> "\n", "\\r" -> "\r")
    """(\\[\\nr])""".r
      .replaceAllIn(value, m => table(m.group(1)))
  }
  /**
   * 設定ファイルを格納するディレクトリ（デフォルトでは./config）から指定したファイルを読み込み、
   * ハッシュリファレンスとして取得します。第一引数にはwikiを渡し、第二引数でファイル名を指定します。
   * {{{
   * val hashref = WikiUtil.loadConfigHash(wiki, &quot;hoge.dat&quot;)
   * }}}
   */
  def loadConfigHash(wiki: AbstractWiki, filename: String): HashMap[String, String] = {
    val text = loadConfigText(wiki, filename)
    val lines: List[String] = text.split("\n").toList
    val unquote = """^\"\(.*\)\"$""".r

    Logger.trace(s"lines => $lines")
    val hash: scala.collection.immutable.HashMap[String, String] = HashMap(lines.map {
      line => trim(line)
    }.filterNot {
      // ignore comment and new-lines
      line => line.startsWith("#") || line == "\n" || line == "\r" || line == "\r\n"
    }.map { line =>
      Logger.trace(s"lines => $line")
      if (line.matches("""^"\(.*\)"$""")) {
        // 1) If "KEY" only exist, un-quote and value is empty
        // 2) Quoted \"\" -> \"
        """^"\(.*\)"$""".r.replaceAllIn(line, m => m.group(1)).replaceAll("\"\"", "\"") -> ""
      } else {
        line.split("=")(0) -> line.split("=")(1)
      }
    }.map {
      case (name, rawValue) =>
        val value = rawValue.replaceAll("^\"|\"$", "")
        Logger.trace(s"name => $name, value => $value")
        unescape(name).trim -> unescape(value).trim
    }.toSeq: _*)

    hash
  }
  /**
   * 設定ファイルを格納するディレクトリ（デフォルトでは./config）から指定したファイルを読み込み、
   * ファイル内容を文字列として取得します。第一引数にはwikiを渡し、第二引数でファイル名を指定します。
   * {{{
   * val content = WikiUtil.loadConfigText(wiki, &quot;hoge.dat&quot;)
   * }}}
   */
  def loadConfigText(wiki: AbstractWiki, filename: String): String = {
    val fullpath = wiki.config("config_dir").getOrElse("./config_dir") + s"/${filename}"
    val file: File = new File(fullpath)
    if (!file.exists) "" // there is no such a file

    wiki.configCache.get(fullpath) match {
      case Some(buf) =>
        buf
      case None =>
        val buf = scala.io.Source.fromFile(file).mkString("")
        wiki.configCache.put(fullpath, buf)
        buf
    }
  }
  /**
   *
   * {{{
   *
   * }}}
   */
  def saveConfigHash(filename: String, hash: HashMap[String, String]) = {
  }
  /**
   *
   * {{{
   *
   * }}}
   */
  def saveConfigText() = {}
  /**
   *
   * {{{
   *
   * }}}
   */
  def syncUpdateConfig() = {}
  /**
   *
   * {{{
   *
   * }}}
   */
  def privatemakeQuotedText() = {}
  /**
   *
   * {{{
   *
   * }}}
   */
  def fileLock() = {}
  /**
   *
   * {{{
   *
   * }}}
   */
  def fileUnlock() = {}
  /**
   *
   * {{{
   *
   * }}}
   */
  def inlineError(message: String, format: String = "WIKI"): String = {
    "Error"
  }
  /**
   *
   * {{{
   *
   * }}}
   */
  def paragraphError(message: String, format: String = "WIKI"): String = {
    "Error"
  }
  /**
   *
   * {{{
   *
   * }}}
   */
  def getResponse(wiki: AbstractWiki, image: String) = {
    ""
  }
  /**
   *
   * {{{
   *
   * }}}
   */
  def getModuleFile(module: String): Option[File] = {

    Logger.debug(s"install plugin: $module")

    Try {
      val resource: String = getClass.getResource(module).getPath
      new File(resource)
    } match {
      case Success(file: File) =>
        Some(file)
      case Failure(e) =>
        Logger.error(e.getMessage)
        None
    }
  }
  /**
   *
   * {{{
   *
   * }}}
   */
  def debug() = {}
  /**
   * java.security.MessageDigestを用いたパスワードの暗号化を行います。
   * 第一引数にパスワード、第二引数にアカウントを渡します。
   *
   * {{{
   * val md5pass = WikiUtil.md5(pass, account)
   * }}}
   */
  def md5(pass: String, salt: String) = {
    val md5 = java.security.MessageDigest.getInstance("MD5")
    md5.update(salt.getBytes)
    md5.digest(pass.getBytes)
      .map("%02x".format(_))
      .mkString
  }
  /**
   *
   * {{{
   *
   * }}}
   */
  def makeContentDisposition() = {}
  /**
   *
   * {{{
   *
   * }}}
   */
  def die() = {}
  /**
   *
   * {{{
   *
   * }}}
   */
  def exit() = {}
  /**
   *
   * {{{
   *
   * }}}
   */
  def restoreDie() = {}
  /**
   *
   * {{{
   *
   * }}}
   */
  def rmtree(file: File): Boolean = {
    org.apache.commons.io.FileUtils.deleteQuietly(file)
  }
  /**
   * Unixのls的な関数、ディレクトリを渡すと再帰的に
   * ディレクトリを探してList[File]形式で返す。
   * @seealso http://hwada.hatenablog.com/entry/20100916/1284625639
   *
   * {{{
   * val files: List[File] = WikiUtil.glob("/var/www/html/")
   * }}}
   */
  def ls(dir: String): List[File] = {
    Option(new File(dir)) match {
      case Some(files) if (Option(files.listFiles).isDefined) =>
        files.listFiles.toList.flatMap {
          case f if f.isDirectory => ls(f.getPath)
          case x => List(x)
        }
      case Some(files) =>
        List.empty[File]
      case None =>
        List.empty[File]
    }
  }
  /**
   * Perlのglob的な関数、ディレクトリを渡すと再帰的に存在する
   * ディレクトリとファイルを探してList[String]形式で返す。
   *
   * {{{
   * // To find files and directories
   * val files: List[String] = WikiUtil.glob("/var/www/html/")
   * // Only to find directories
   * val files: List[String] = WikiUtil.glob("/var/www/html/", true)
   * }}}
   */
  def glob(dir: String, onlyDir: Boolean = false): List[String] = {
    val files: List[File] = ls(dir)
    val pathes: List[String] = files.collect {
      case f: File => f.getPath
    }
    val dirs: List[String] = files.sorted.map {
      file => file.getParent
    }.distinct

    if (onlyDir) {
      dirs.sorted
    } else {
      (pathes ++ dirs).sorted
    }
  }
}
