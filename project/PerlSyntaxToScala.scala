import com.google.common.base.CaseFormat._

trait PerlSyntaxToScala {

  val dollarDef = """(.*:?)my \$(.*)""".r
  val dollarOnly = """(.*:?)\$(.*:?)=(.*)""".r
  val hashDef = """(.*:?)my %(.*)$""".r
  val hashUse = """(.*)([a-zA-Z_]*)\{([a-zA-Z_]*)\}(.*)""".r

  val at = """(.*:?)my @(.*:?)=(.*)""".r
  val statement = """(.*)[ eq ][ ne ](.*)""".r
  val wiki = """(.*:?)\$wiki->(.*:?)(\(.*)""".r

  val replaceWiki = (perl: String) =>
  perl match {
    case wiki(head, method, tail) =>
      val sMethod = LOWER_UNDERSCORE.to(UPPER_CAMEL, method)
      s"${head}wiki.${sMethod}${tail}"
    case _ =>
      perl
  }

  val replaceStatement = (perl: String) =>
  perl match {
    case statement(head, tail) if (perl.contains("eq"))=>
      s"${head} == ${tail}"
    case statement(head, tail) if (perl.contains("ne"))=>
      s"${head} != ${tail}"
    case _ =>
      perl
  }

  val replaceDefDollarMark = (perl: String) =>
  perl match {
    case dollarDef(head, tail) =>
      s"${head}val ${tail}"
    case _ =>
      perl
  }

  val replaceDefHash = (perl: String) =>
  perl match {
    case hashDef(head, variable) =>
      s"${head}val ${variable}: scala.collection.mutable.HashMap[String, String]"
    case _ =>
      perl
  }

  val replaceUseHash = (perl: String) =>
  perl match {
    case hashUse(head, hash, key, tail) =>
      s"""${head}${hash}(\"${key}\")${tail}"""
    case _ =>
      perl
  }

  val replaceOnlyDollarMark = (perl: String) =>
  perl match {
    case dollarOnly(head, variable, tail) =>
      s"${head}${variable}=${tail}"
    case _ =>
      perl
  }

  val replaceAtMark = (perl: String) =>
  perl match {
    case at(head, variable, tail) =>
      s"${head}val ${variable}: Array[String] = ${tail}"
    case _ =>
      perl
  }

  val replaceValDef = replaceDefDollarMark
    .andThen(replaceAtMark)
    .andThen(replaceOnlyDollarMark)
    .andThen(replaceDefHash)
    .andThen(replaceUseHash)
    .andThen(replaceStatement)
    .andThen(replaceWiki)

  def perlSyntaxToScala(line: String): String = {

    val syntaxModified: String = replaceValDef(line)
    syntaxModified
      .replaceAll("->", ".")
      .replaceAll("#", "//")
      .replaceAll(";", "")
      .replaceAll("return ", "")
  }
}
