trait PerlSyntaxToScala {

  val dollarDef = """(.*:?)my \$(.*)""".r
  val dollarOnly = """(.*:?)\$(.*:?)=(.*)""".r
  val at = """(.*:?)my @(.*:?)=(.*)""".r

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

  def perlSyntaxToScala(line: String): String = {

    val syntaxModified: String = replaceValDef(line)
    syntaxModified.replaceAll("#", "//").replaceAll(";", "")
  }
}
