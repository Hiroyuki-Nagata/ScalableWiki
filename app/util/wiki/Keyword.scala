package jp.gr.java_conf.hangedman.util.wiki

class Keyword {

  // Regex matches a char
  // FIXME <-- UTF-8にしないとダメじゃない？
  val ascii = """[\x00-\x7F]""".r // ASCIIの 1 文字
  val twoBytes = """[\x8E\xA1-\xFE][\xA1-\xFE]""".r // EUC 2 Byte の 1 文字
  val threeBytes = """\x8F[\xA1-\xFE][\xA1-\xFE]""".r // EUC 3 Byte の 1 文字
  val AsciiOrEUC = s"""$ascii|$twoBytes|$threeBytes""" // ASCII/EUC  の 1 文字

  val keyword_cache = "keywords.cache" // 古いキーワードキャッシュファイル
  val keyword_cache2 = "keywords2.cache" // 新しいキーワードキャッシュファイル

  def existsKeyword() = {}
  def loadKeywords() = {}
  def saveKeywords() = {}
  def parse() = {}
  def parseLine() = {}
  def urlAnchor() = {}
  def wikiAnchor() = {}

}
