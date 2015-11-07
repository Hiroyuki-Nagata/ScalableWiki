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

  val rewriteIfStatement = (statement: String, defaultElem: String) =>
  """@if \((.*?)\)""".r
    .replaceAllIn(statement, m => s"@if (${defaultElem}" + m.group(1) + ")")

  val tmplVarToScala = (perlString: String) =>
  """<!--TMPL_VAR NAME=\"(.*?)\".*-->""".r
    .replaceAllIn(perlString, m => "@" + m.group(1))

  val tmplIfToScala = (perlString: String) =>
  // for one line
  """<!--TMPL_IF NAME=\"(.*?)\".*-->(.*?)<!--/TMPL_IF-->""".r
    .replaceAllIn(perlString, m => "@if (" + m.group(1) + ".nonEmpty) { " + m.group(2) + " } ")

  val tmplIfBeginToScala = (perlString: String) =>
  // for multi lines
  """<!--TMPL_IF NAME=\"(.*?)\".*-->""".r
    .replaceAllIn(perlString, m => "@if (" + m.group(1) + ".nonEmpty) { ")

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

  val tmplIfMultiLineToScala = (perlList: List[String]) => 
  removeMultiLineIfStatement(perlList): List[String]

  val tmplMultiLinesToScala = tmplLoopToScala.andThen(tmplIfMultiLineToScala)
}
