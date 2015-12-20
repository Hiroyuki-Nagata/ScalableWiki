////////////////////////////////////////////////////////////
//
// 添付ファイルのアクションハンドラ。
//
////////////////////////////////////////////////////////////
package jp.gr.java_conf.hangedman.plugin.attach

import jp.gr.java_conf.hangedman.plugin.attach._
import jp.gr.java_conf.hangedman.util.WikiUtil
import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
import java.io.File
import play.Logger
import scala.util.{ Failure, Success, Try }

//===========================================================
// コンストラクタ
//===========================================================
class AttachHandler(className: String, tpe: WikiPluginType, format: WikiFormat)
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

  //===========================================================
  // アクションの実行
  //===========================================================
  def doAction(wiki: AbstractWiki): String = {
    val cgi = wiki.getCGI
    val pagename = if (cgi.paramPage("page").isEmpty) {
      wiki.config("frontpage") match {
        case Some(frontpage) => frontpage
        case None => ""
      }
    } else {
      cgi.paramPage("page")
    }

    wiki.setTitle("ファイルの添付", true)

    if (cgi.getParam("UPLOAD").nonEmpty ||
      cgi.getParam("CONFIRM").nonEmpty ||
      cgi.getParam("DELETE").nonEmpty) {
      if (!wiki.canModifyPage(pagename)) {
        wiki.error("編集は禁止されています。")
      }
    }

    if (cgi.getParam("DELETE").nonEmpty) {
      ""
      /**
       * if (!plugin :: attach :: Files :: can_attach_delete(wiki, pagename)) {
       * wiki.error("ファイルの削除は許可されていません。")
       * }
       */
    }

    ""
  }
  /**
   * //-------------------------------------------------------
   * // アップロード実行
   * if(cgi.getParam("UPLOAD") != ""){
   * val filename = cgi.getParam("file")
   * filename =~ s/\\/\//g
   * filename = substr(filename,rindex(filename,"/")+1)
   * filename =~ tr/"\x00-\x1f/': /
   * Jcode::convert(\filename,"euc")
   *
   * if(filename == ""){
   * wiki.error("ファイルが指定されていません。")
   * }
   *
   * val hundle = cgi.upload("file")
   * if(!hundle){
   * wiki.error("ファイルが読み込めませんでした。")
   * }
   *
   * val uploadfile = wiki.config("attachDir") + "/" + util::urlEncode(pagename) + " + " + util::urlEncode(filename)
   * if (new File(uploadfile  !plugin::attach::Files::can_attach_update(wiki, pagename)).exists){
   * wiki.error("ファイルの上書きは許可されていません。")
   * }
   *
   * open(DATA,">uploadfile") or die !
   * binmode(DATA)
   * while(read(hundle,_,16384)){ print DATA _ }
   * close(DATA)
   *
   * // attachプラグインから添付された場合
   * if(defined(cgi.getParam("count"))){
   * val lines : Array[String] =  split(/\n/,wiki.getPage(pagename))
   * val flag = 0
   * val form_count = 1
   * val count=cgi.getParam("count")
   * val content = ""
   * lines.foreach { _ =>
   * if(index(_," ")==0||index(_,"\t")==0){
   * content += _ + "\n"
   * next
   * }
   * if(index(_,"{("attach")}")!=-1  flag==0){
   * if(form_count==count){
   * content = content + "{{ref " + filename + "}}\n"
   * flag = 1
   * } else {
   * form_count++
   * }
   * }
   * content = content._ + "\n"
   * }
   * if(flag==1){
   * wiki.savePage(pagename,content)
   * }
   * }
   *
   * // ログの記録
   * write_log(wiki,"UPLOAD",pagename,filename)
   *
   * wiki.redirect(pagename)
   *
   * //-------------------------------------------------------
   * // 削除確認
   * } else if(cgi.getParam("CONFIRM") != ""){
   * val file = cgi.getParam("file")
   * if(file == ""){
   * wiki.error("ファイルが指定されていません。")
   * }
   *
   * val buf = ""
   *
   * buf += "<a href=\"" + wiki.createPageUrl(pagename) + "\">" +
   * Util::escapeHTML(pagename) + "</a>から" + WikiUtil.escapehtml(file) + "を削除してよろしいですか？\n" +
   * "<form action=\"" + wiki.create_url() + "\" method=\"POST\">\n" +
   * "  <input type=\"submit\" name=\"DELETE\" value=\"削 除\">" +
   * "  <input type=\"hidden\" name=\"action\" value=\"ATTACH\">" +
   * "  <input type=\"hidden\" name=\"page\" value=\"" + WikiUtil.escapehtml(pagename) + "\">" +
   * "  <input type=\"hidden\" name=\"file\" value=\"" + WikiUtil.escapehtml(file) + "\">" +
   * "</form>"
   * buf
   *
   * //-------------------------------------------------------
   * // 削除実行
   * } else if(cgi.getParam("DELETE") != ""){
   * val file = cgi.getParam("file")
   * if(file == ""){
   * wiki.error("ファイルが指定されていません。")
   * }
   *
   * // ログの記録
   * write_log(wiki,"DELETE",pagename,file)
   *
   * unlink(wiki.config("attachDir") + "/" + util::urlEncode(pagename) + " + " + util::urlEncode(file))
   * wiki.redirect(pagename)
   *
   * //-------------------------------------------------------
   * // ダウンロード
   * } else {
   * val file = cgi.getParam("file")
   * if(file == ""){
   * wiki.error("ファイルが指定されていません。")
   * }
   * if(!wiki.pageExists(pagename)){
   * wiki.error("ページが存在しません。")
   * }
   * if(!wiki.canShow(pagename)){
   * wiki.error("ページの参照権限がありません。")
   * }
   * val filepath = wiki.config("attachDir") + "/" + util::urlEncode(pagename) + " + " + util::urlEncode(file)
   * if(!-e filepath){
   * wiki.error("ファイルがみつかりません。")
   * }
   *
   * val contenttype = get_mime_type(wiki,file)
   * val ua = ENV{"HTTP_USER_AGENT"}
   * val disposition = (contenttype =~ /^image\//  ua !~ /MSIE/ ? "inline" : "attachment")
   *
   * open(DATA, filepath) or die !
   * print("Content-Type: contenttype\n")
   * print WikiUtil.makeContentDisposition(file, disposition)
   * binmode(DATA)
   * while(read(DATA,_,16384)){ print _ }
   * close(DATA)
   *
   * // ログの記録
   * write_log(wiki,"DOWNLOAD",pagename,file)
   * count_up(wiki,pagename,file)
   *
   * exit()
   * }
   * }
   *
   * //===========================================================
   * // ダウンロードカウントをインクリメント
   * //===========================================================
   * def countUp(wiki: AbstractWiki, page: String, file: String): String = {
   * WikiUtil.syncUpdateConfig(undef,wiki.config('logDir') + "/" + wiki.config("download_count_file"),
   * sub {
   * val hash = shift
   * if(!defined(hash.{page + "::" + file})){
   * hash.{page + "::" + file} = 1
   * } else {
   * hash.{page + "::" + file}++
   * }
   * hash
   * }
   * )
   * }
   *
   * //===========================================================
   * // 添付ファイルのログ
   * //===========================================================
   * def writeLog(){
   * val mode = shift
   * val page = shift
   * val file = shift
   * if(wiki.config('log_dir') == "" || wiki.config("attach_log_file") == ""){
   * return
   * }
   * val ip  = ENV{"REMOTE_ADDR"}
   * val ref = ENV{"HTTP_REFERER"}
   * val ua  = ENV{"HTTP_USER_AGENT"}
   * if(ip  == ""){ ip  = "-" }
   * if(ref == ""){ ref = "-" }
   * if(ua  == ""){ ua  = "-" }
   * my (sec, min, hour, mday, mon, year) = localtime(time())
   * val date = sprintf("%04d/%02d/%02d %02d:%02d:%02d",year+1900,mon+1,mday,hour,min,sec)
   *
   * val logfile = wiki.config('log_dir') + "/" + wiki.config("attach_log_file")
   * WikiUtil.fileLock(logfile)
   * open(LOG,">>logfile") or die !
   * binmode(LOG)
   * print LOG mode + " " + Util::url_encode(page) + " " + WikiUtil.urlEncode(file) + " " +
   * date + " " + ip + " " + ref + " " + ua + "\n"
   * close(LOG)
   * WikiUtil.fileUnlock(logfile)
   * }
   *
   * //===========================================================
   * // MIMEタイプを取得します
   * //===========================================================
   * def getMimeType(): String = {
   * val file = shift
   * val type = lc(substr(file,rindex(file," + ")+1))
   *
   * val hash  = WikiUtil.loadConfigHash(wiki,wiki.config("mime_file"))
   * val ctype = hash.{type}
   *
   * if(ctype == "" ){
   * ctype = "application/octet-stream"
   * }
   *
   * ctype
   * }
   */
}
