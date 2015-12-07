import com.google.common.base.CaseFormat._

trait PerlSyntaxToScala extends CommonTrait {

  val dollarDef = """(.*:?)my \$(.*)""".r
  val dollarOnly = """(.*:?)\$(.*:?)=(.*)""".r
  val hashDef = """(.*:?)my %(.*)$""".r
  val hashUse = """(.*)([a-zA-Z_]*)\{([a-zA-Z_]*)\}(.*)""".r
  val printDef = """(.*:?)print (\".*\":?)(.*)$""".r
  val singleQuoteDef = """(.*:?)'(.*:?)'(.*)""".r
  //val xmlLiteralBegin = """(.*:?)qq\|(.*)""".r

  val at = """(.*:?)my @(.*:?)=(.*)""".r
  val statement = """(.*)[ eq ][ ne ](.*)""".r
  val foreachDef = """(.*:?)foreach val (.*:?)\(@(.*:?)\)\{(.*)$""".r
  val foreachLoop = """(.*:?)foreach\(@(.*:?)\)\{(.*)$""".r
  val wiki = """(.*:?)\$wiki->(.*:?)(\(.*)""".r

  val replaceSimpleForeach = (perl: String) =>
  perl match {
    case foreachDef(head, element, collection, tail) =>
      s"""${head}${collection}.foreach { ${element} => ${tail}"""
    case foreachLoop(head, collection, tail) =>
      s"""${head}${collection}.foreach { _ => ${tail}"""
    case _ =>
      perl
  }

  // val replaceXmlLiteral = (perl: String) =>
  // perl match {
  //   case xmlLiteralBegin(head, tail) =>
  //     s"""${head}${tail}"""
  //   case _ =>
  //     perl
  // }

  val replaceSingleQuoteDef = (perl: String) =>
  perl match {
    case singleQuoteDef(head, quoted, tail) =>
      s"""${head}\"${quoted}\"${tail}"""
    case _ =>
      perl
  }

  val replacePrintDef = (perl: String) =>
  perl match {
    case printDef(head, message, tail) =>
      val modified = message.replaceAll("\"\\.", "\" + ").replaceAll("\\.\"", " + \"")
      s"${head}print(${modified})${tail}"
    case _ =>
      perl
  }

  val replaceWiki = (perl: String) =>
  perl match {
    case wiki(head, method, tail) =>
      s"${head}wiki.${toCamel(method)}${tail}"
    case _ =>
      perl
  }

  val replaceStatement = (perl: String) =>
  perl match {
    case statement(head, tail) if (perl.contains("eq"))=>
      perl.replaceAll(" eq ", " == ")
    case statement(head, tail) if (perl.contains("ne"))=>
      perl.replaceAll(" ne ", " != ")
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
    .andThen(replacePrintDef)
    .andThen(replaceSingleQuoteDef)
    .andThen(replaceSimpleForeach)

  def perlSyntaxToScala(line: String): String = {

    val syntaxModified: String = replaceValDef(line)
    syntaxModified
      .replaceAll("->", ".")
      .replaceAll("#", "//")
      .replaceAll(";", "")
      .replaceAll("return ", "")
      .replaceAll("elsif", "else if")
      .replaceAll("\"\\.", "\" + ")
      .replaceAll("\\.\"", " + \"")
      .replaceAll("\\.=", "+=")
      .replaceAll("""\$""", "")
      .replaceAll("self\\.", "this\\.")
  }
}
