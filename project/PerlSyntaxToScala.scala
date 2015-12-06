import com.google.common.base.CaseFormat._

trait PerlSyntaxToScala {

  val dollarDef = """(.*:?)my \$(.*)""".r
  val dollarOnly = """(.*:?)\$(.*:?)=(.*)""".r
  val at = """(.*:?)my @(.*:?)=(.*)""".r
  val statement = """(.*)[ eq ][ ne ](.*)""".r
  val wiki = """(.*:?)\$wiki->(.*:?)(\(.*)""".r

  val replaceWiki = (perl: String) =>
  perl match {
    case wiki(head, method, last) =>
      val sMethod = LOWER_UNDERSCORE.to(UPPER_CAMEL, method)
      s"${head}wiki.${sMethod}${last}"
    case _ =>
      perl
  }

  val replaceStatement = (perl: String) =>
  perl match {
    case statement(head, last) if (perl.contains("eq"))=>
      s"${head} == ${last}"
    case statement(head, last) if (perl.contains("ne"))=>
      s"${head} != ${last}"
    case _ =>
      perl
  }

  val replaceDefDollarMark = (perl: String) =>
  perl match {
    case dollarDef(head, last) =>
      s"${head}val ${last}"
    case _ =>
      perl
  }

  val replaceOnlyDollarMark = (perl: String) =>
  perl match {
    case dollarOnly(head, variable, last) =>
      s"${head}${variable}=${last}"
    case _ =>
      perl
  }

  val replaceAtMark = (perl: String) =>
  perl match {
    case at(head, variable, last) =>
      s"${head}val ${variable}: Array[String] = ${last}"
    case _ =>
      perl
  }

  val replaceValDef = replaceDefDollarMark
    .andThen(replaceAtMark)
    .andThen(replaceOnlyDollarMark)
    .andThen(replaceStatement)
    .andThen(replaceWiki)

  def perlSyntaxToScala(line: String): String = {

    val syntaxModified: String = replaceValDef(line)
    syntaxModified
      .replaceAll("->", ".")
      .replaceAll("#", "//")
      .replaceAll(";", "")
      .replaceAll("return $", "")
      .replaceAll("return ", "")
      .replaceAll("$", "")
  }
}
