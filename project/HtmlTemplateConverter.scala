trait HtmlTemplateConverter {

  def removeLoopStatement(lines: List[String]): List[String] = {

    val p = """.*<!--TMPL_LOOP NAME=\"(.*?)\".*-->.*""".r

    lines.indexWhere(line => line.contains("<!--TMPL_LOOP NAME")) match {
      case index if (index != -1) =>
        val name = lines(index) match { case p(name) => s"$name" }
        val restOfLines: List[String] = lines.drop(index+1)
        val endOfLoop: Int = restOfLines.indexWhere(line => line.contains("<!--/TMPL_LOOP-->"))
        val internalLoop: List[String] = restOfLines.take(endOfLoop).zipWithIndex.map {
          case(line: String, index: Int) =>
            if (index < endOfLoop) {
              line.contains("@if") match {
                case false =>
                  line.replace("@", "@e.")
                case true =>
                  rewriteIfStatement(line, "e.")
              }
            } else {
              line
            }
        }

        lines.take(index) ++
        List( tmplLoopBeginToScala( lines(index) ) ) ++
        internalLoop ++
        List( tmplLoopEndToScala( restOfLines(endOfLoop))) ++
        restOfLines.drop(endOfLoop + 1)

      case _ =>
        lines

    }
  }

  val rewriteIfStatement = (statement: String, defaultElem: String) =>
  """@if \((.*?)\)""".r
    .replaceAllIn(statement, m => s"@if (${defaultElem}" + m.group(1) + ")")

  val tmplVarToScala = (perlString: String) =>
  """<!--TMPL_VAR NAME=\"(.*?)\".*-->""".r
    .replaceAllIn(perlString, m => "@" + m.group(1))

  val tmplIfToScala = (perlString: String) =>
  """<!--TMPL_IF NAME=\"(.*?)\".*-->(.*?)<!--/TMPL_IF-->""".r
    .replaceAllIn(perlString, m => "@if (" + m.group(1) + ".nonEmpty) { " + m.group(2) + " } ")

  val tmplUnlessToScala = (perlString: String) =>
  """<!--TMPL_UNLESS NAME=\"(.*?)\".*-->(.*?)<!--/TMPL_UNLESS-->""".r
    .replaceAllIn(perlString, m => "@if (" + m.group(1) + ".isEmpty) { " + m.group(2) + " } ")

  val tmplIfElseToScala = tmplIfToScala.andThen(tmplUnlessToScala)

  val tmplVarAndIfElseToScala = tmplVarToScala.andThen(tmplIfElseToScala)

  val tmplLoopToScala = (perlList: List[String]) =>
  removeLoopStatement(perlList): List[String]

  val tmplLoopBeginToScala = (perlString: String) =>
  """<!--TMPL_LOOP NAME=\"(.*?)\".*-->""".r
    .replaceAllIn(perlString, m => "@for(e <- " + m.group(1) + ") {")

  val tmplLoopEndToScala = (perlString: String) =>
  """<!--/TMPL_LOOP-->""".r
    .replaceAllIn(perlString, m => "} ")

  val tmplIfMultiLineToScala = (perlList: List[String]) => perlList: List[String]

  val tmplMultiLinesToScala = tmplLoopToScala.andThen(tmplIfMultiLineToScala)
}
