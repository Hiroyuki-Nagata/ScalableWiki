/*
 * This program is written for converting Perl HTML::Template format text
 * to Scala template's one.
 *
 * Copyright (C) 2015 Hiroyuki Nagata
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributor:
 *	Hiroyuki Nagata <idiotpanzer@gmail.com>
 */
trait HtmlTemplateConverter {

  @scala.annotation.tailrec
  private def removeLoopStatement(lines: List[String]): List[String] = {

    val p = """.*<!--TMPL_LOOP NAME=\"(.*?)\".*-->.*""".r

    lines.indexWhere(line => line.contains("<!--TMPL_LOOP NAME")) match {

      case index if (index != -1) =>
        // Get $name, <!--TMPL_LOOP NAME="$name"-->
        val name = lines(index) match { case p(name) => s"$name" }
        // Check after TMPL_LOOP tag
        val restOfLines: List[String] = lines.drop(index+1)
        // Get end index of <!--/TMPL_LOOP-->
        val endOfLoop: Int = restOfLines.indexWhere(line => line.contains("<!--/TMPL_LOOP-->"))
        // Rewrite internal LOOP tag
        val internalLoop: List[String] = restOfLines.take(endOfLoop).zipWithIndex.map {

          case(line: String, index: Int) if (index < endOfLoop) =>
            line.contains("@if") match {
              case false =>
                line.replace("@", "@e.")
              case true =>
                rewriteIfStatement(line, "e.")
            }
          case(line: String, _) if (index >= endOfLoop) =>
            line
        }.map {
          // Rewrite; <!--TMPL_VAR ESCAPE="HTML" NAME="VALUE"-->
          line => rewriteVarStatement(line, "e.")
        }

        // Recursion ! Recursion !
        removeLoopStatement(
          lines.take(index) ++
            List( tmplLoopBeginToScala( lines(index) ) ) ++
            internalLoop ++
            List( tmplLoopEndToScala( restOfLines(endOfLoop))) ++
            restOfLines.drop(endOfLoop + 1)
        )

      // This is end ! Closing !
      case _ =>
        lines
    }
  }

  @scala.annotation.tailrec
  private def removeMultiLineIfStatement(lines: List[String]): List[String] = {

    val p = """.*<!--TMPL_IF NAME=\"(.*?)\".*-->.*""".r

    lines.indexWhere(line => line.contains("<!--TMPL_IF NAME")) match {

      case index if (index != -1) =>
        // Get $name, <!--TMPL_IF NAME="$name"-->
        val name = lines(index) match { case p(name) => s"$name" }
        // Check after TMPL_IF tag
        val restOfLines: List[String] = lines.drop(index+1)
        // Get end index of <!--/TMPL_IF-->
        val endOfIf: Int = restOfLines.indexWhere(line => line.contains("<!--/TMPL_IF-->"))

        // Recursion ! Recursion !
        removeMultiLineIfStatement(
          lines.take(index) ++
            List( tmplIfBeginToScala( lines(index) ) ) ++
            restOfLines.take(endOfIf) ++
            List( restOfLines(endOfIf).replace("""<!--/TMPL_IF-->""", "}") ) ++
            restOfLines.drop(endOfIf + 1)
        )

      // This is end ! Closing !
      case _ =>
        lines
    }
  }

  @scala.annotation.tailrec
  private def removeMultiLineUnlessStatement(lines: List[String]): List[String] = {

    val p = """.*<!--TMPL_UNLESS NAME=\"(.*?)\".*-->.*""".r

    lines.indexWhere(line => line.contains("<!--TMPL_UNLESS NAME")) match {

      case index if (index != -1) =>
        // Get $name, <!--TMPL_UNLESS NAME="$name"-->
        val name = lines(index) match { case p(name) => s"$name" }
        // Check after TMPL_UNLESS tag
        val restOfLines: List[String] = lines.drop(index+1)
        // Get end index of <!--/TMPL_UNLESS-->
        val endOfUnless: Int = restOfLines.indexWhere(line => line.contains("<!--/TMPL_UNLESS-->"))

        // Recursion ! Recursion !
        removeMultiLineUnlessStatement(
          lines.take(index) ++
            List( tmplUnlessBeginToScala( lines(index) ) ) ++
            restOfLines.take(endOfUnless) ++
            List( restOfLines(endOfUnless).replace("""<!--/TMPL_UNLESS-->""", "}") ) ++
            restOfLines.drop(endOfUnless + 1)
        )

      // This is end ! Closing !
      case _ =>
        lines
    }
  }

  val rewriteIfStatement = (statement: String, defaultElem: String) =>
  """@if \((.*?)\)""".r
    .replaceAllIn(statement, m => s"@if(${defaultElem}" + m.group(1) + ")")

  val rewriteVarStatement = (statement: String, defaultElem: String) =>
  """<!--TMPL_VAR(?:.*)NAME=\"(.*?)\".*-->""".r
    .replaceAllIn(statement, m => "@" + defaultElem + m.group(1))

  val tmplVarToScala = (perlString: String) => {

    val temp = """<!--TMPL_VAR(?:.*)NAME=\"CONTENT\".*-->""".r
      .replaceAllIn(perlString, m => "@Html(CONTENT)")

    """<!--TMPL_VAR(?:.*)NAME=\"(.*?)\".*-->""".r
      .replaceAllIn(temp, m => "@" + m.group(1))
  }

  val tmplIfToScala = (perlString: String) =>
  // for one line
  """<!--TMPL_IF.*NAME=\"(.*?)\".*-->(.*?)<!--/TMPL_IF-->""".r
    .replaceAllIn(perlString, m => "@if(" + m.group(1) + ".nonEmpty) { " + m.group(2) + " } ")

  val tmplIfBeginToScala = (perlString: String) =>
  // for multi lines
  """<!--TMPL_IF.*NAME=\"(.*?)\".*-->""".r
    .replaceAllIn(perlString, m => "@if(" + m.group(1) + ".nonEmpty) { ")

  val tmplUnlessToScala = (perlString: String) =>
  // for one line
  """<!--TMPL_UNLESS.*NAME=\"(.*?)\".*-->(.*?)<!--/TMPL_UNLESS-->""".r
    .replaceAllIn(perlString, m => "@if(" + m.group(1) + ".isEmpty) { " + m.group(2) + " } ")

  val tmplUnlessBeginToScala = (perlString: String) =>
  // for multi lines
  """<!--TMPL_UNLESS.*NAME=\"(.*?)\".*-->""".r
    .replaceAllIn(perlString, m => "@if(" + m.group(1) + ".isEmpty) { ")

  val tmplIfElseToScala = tmplIfToScala.andThen(tmplUnlessToScala)

  val tmplVarAndIfElseToScala = tmplIfElseToScala.andThen(tmplVarToScala)

  val tmplLoopToScala = (perlList: List[String]) =>
  removeLoopStatement(perlList): List[String]

  val tmplLoopBeginToScala = (perlString: String) =>
  """<!--TMPL_LOOP NAME=\"(.*?)\".*-->""".r
    .replaceAllIn(perlString, m => "@for(e <- " + m.group(1) + ") {")

  val tmplLoopEndToScala = (perlString: String) =>
  """<!--/TMPL_LOOP-->""".r
    .replaceAllIn(perlString, m => "} ")

  val tmplIfElseMultiLineToScala = (perlList: List[String]) =>
  (removeMultiLineIfStatement _ andThen removeMultiLineUnlessStatement _)(perlList): List[String]

  val tmplMultiLinesToScala = tmplLoopToScala.andThen(tmplIfElseMultiLineToScala)

  def isHtmlTag(imp: String): Boolean = {
    imp match {
      case "HEAD_INFO" | "FOOTER" =>
        true
      case _ =>
        false
    }
  }

  def getScalaTemplateArguments(formatted: List[String])(implicit gen: GenerateCaseClasses): List[String] = {

    val captureRegex = """.*?(@[A-Z_]*?)[^A-Z_].*$"""
    val trimRegex = """^(?:.*?)(@.*)$"""
    val atIfRegex = """@if\(([A-Za-z0-9_]*?)(?:\.isEmpty|\.nonEmpty)\).*$"""
    val atForRegex = """@for\(.* ([A-Za-z0-9_]*?)\).*$"""
    val atERegex = """@e\.([A-Za-z0-9_]*?)(?:$|[^A-Za-z0-9_]+.*$)"""

    import scala.collection.mutable.HashMap
    import scala.collection.mutable.MultiMap
    import scala.collection.mutable.Set
    val forLoopMap = new HashMap[String, Set[String]] with MultiMap[String, String]
    var forLoopKey = ""

    formatted.filter {
      line => line.contains("@")
    }.map {
      _ match {
        case atIf if (atIf.contains("@if")) =>
          atIf.trim.replaceAll(trimRegex, "$1").replaceAll(atIfRegex, "$1")
        case atFor if (atFor.contains("@for")) =>
          forLoopKey = atFor.trim.replaceAll(trimRegex, "$1").replaceAll(atForRegex, "$1")
          forLoopKey
        case atE if (atE.contains("@e.")) =>
          val forLoopValue = atE.trim.replaceAll(trimRegex, "$1").replaceAll(atERegex, "$1")
          if (forLoopKey.nonEmpty) {
            forLoopMap.addBinding(forLoopKey, forLoopValue)
          }
          ""
        case line if (line.trim.contains("@Html(CONTENT)"))=>
          // I would like to ignore "@Html(FOO)" tag
          // A below element is a argument of template files
          "CONTENT"
        case line =>
          line.trim.replaceAll(trimRegex, "$1")
      }
    }.map {
      line => line.replaceAll(captureRegex, "$1")
    }.distinct.filterNot {
      line => line.equals("@") || line.isEmpty
    }.map {
      line => line.replace("@", "")
    }.toList.distinct match {
      case imports: List[String] =>
        val classMap: HashMap[String, String] = gen.generateCaseClasses(forLoopMap)
        val classSpecifiedImports = imports.map { imp =>
          if (classMap.contains(imp)) {
            imp + ": " + classMap(imp)
          } else if (isHtmlTag(imp)) {
            imp + ": Html"
          } else {
            imp + ": String"
          }
        }

        println(s"This template file has ${classSpecifiedImports.size} arguments !")
        classSpecifiedImports
    }
  }
}
