import java.io.File
import sbt.Keys._
import sbt._
import com.google.common.base.CaseFormat._

object ConvertPlugins extends CommonTrait with PerlSyntaxToScala {

val installDef = """
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
""".stripMargin

  def clean() {
    val pluginDir = new File("./public/plugin/")
    val fi: Seq[File] = (pluginDir ** "*.pm").get

    fi foreach { x =>
      // /public/plugin/{name}/***.pm -> /app/plugin/{name}/***.scala
      val out = x.getPath.replace("./public/plugin/", "./app/plugin/").replace(".pm", ".scala")
      println("Clean plugin file => " + out)
      val fo: File = new File(out)
      if (fo.exists) fo.delete
    }
  }

  def convert(args: Seq[String]) {
    val pluginDir = new File("./public/plugin/")

    val fi: Seq[File] = (pluginDir ** "*.pm").get.collect {
      case file: File if (args.isEmpty) =>
        file
      case file: File if (args.exists(x => file.getPath.contains(x))) =>
        file
    }

    fi foreach { x =>
      // /public/plugin/{name}/***.pm -> /app/plugin/{name}/***.scala
      val out = x.getPath.replace("./public/plugin/", "./app/plugin/").replace(".pm", ".scala")
      println("Plugin file " + x.getPath + " => " + out)
      val fo: File = new File(out)
      fo.getParentFile.mkdirs
      fo.createNewFile
      import com.google.common.io.Files
      val className: String = Files.getNameWithoutExtension(x.getName)
      val wikiPackage: String = "jp.gr.java_conf.hangedman.util.wiki"
      val ourPackageName: String = "jp.gr.java_conf.hangedman"
      val pluginRegex = """(.*?)(\$wiki.*plugin:?)\((.*:?),(.*:?),(.*:?)\);.*$""".r

      var isHereDoc = false
      var beginClassParen = false

      // load file contents
      printToFile(fo) { p =>
        scala.io.Source.fromFile(x).getLines.toList.map { line =>
          line match {
            /**
              * Process heredoc lines
              */
            case line if (!isHereDoc && line.contains("<<\"EOD\"")) =>
              isHereDoc = true
              p.println("\"\"\"")
            case line if (isHereDoc) =>
              if (line.startsWith("EOD")) {
                isHereDoc = false
                p.println("\"\"\"")
              } else {
                p.println(line)
              }
            /**
              * Process commented lines
              */
            case line if (line.startsWith("#") && line.matches("""^[#]*$""") && line != "#") =>
              p.println(line.replaceAll("#", "/"))
            case line if (line.startsWith("#")) =>
              p.println(line.replaceFirst("#", "//"))
            /**
              * Process import lines
              */
            case line if (line.startsWith("use") && line.endsWith(";")) =>
              p.println("")
            case line if (line.startsWith("package") && line.endsWith(";")) =>
              val fullPackageName: String = line
                .replaceAll("::", ".")
                .replace("package ", "")
                .replace(s".${className};", "")
              p.println(s"package ${ourPackageName}.${fullPackageName}")
              p.println(s"""|
                            |import ${ourPackageName}.plugin._
                            |import ${ourPackageName}.${fullPackageName}._
                            |import ${ourPackageName}.util.WikiUtil
                            |import jp.gr.java_conf.hangedman.model._
                            |import jp.gr.java_conf.hangedman.util.wiki.AbstractWiki
                            |import java.io.File
                            |import play.Logger
                            |import scala.util.{ Failure, Success, Try }
                            |""".stripMargin)
            /**
              * Process class defined lines
              */
            case line if (line.contains("return") && line.contains("bless")) =>
              // do nothing
            case line if (line.contains("$class") && line.contains("shift")) =>
              // do nothing
            case line if (line.contains("my") && line.contains("$self")) =>
              // do nothing
            case line if (line.startsWith("sub") && line.contains("new")) =>
              // replace class definition and define extends "WikiPlugin"
              beginClassParen = true
              val pluginDef = "(className: String, tpe: WikiPluginType, format: WikiFormat)"
              val pluginArg = if (className.contains("handler")) {
                "WikiPlugin(className, tpe, format)"
              } else {
                "WikiHandler(className, tpe, format)"
              }

              p.println(line
                .replace("sub", "class")
                .replace("new", s"${className}${pluginDef}\n    extends ${pluginArg}")
              )
              if (className != "Install" ) {
                p.println(installDef)
              }
            case line if (line.startsWith("sub")) =>
              if (line.contains("install")) {
                p.println(
                  line.replace("sub", s"object ${className} extends InstallTrait {\n")
                    .replace("install", s"def install(wiki: ${wikiPackage}.AbstractWiki)")
                )
              } else {
                // 'sub function {' => 'def function() {'
                val modified: String = line.split(" ") match {
                  case lines if (lines.size >= 2) =>
                    toScalaFunc(line).replace(lines(1), toCamel(lines(1)))
                  case _ =>
                    toScalaFunc(line)
                }
                p.println(modified)
              }
            case line if (line.contains("$wiki") && line.contains("shift")) =>
              // do nothing
            case line if (line.contains("wiki->")) =>
              def wikiPlugin(arg2: String): String = {
                val full: String = s"${ourPackageName}." + arg2.replaceAll("\"", "").replaceAll("::", ".")
                full.split('.') match {
                  case pack if (pack.size < 2) =>
                    full
                  case pack if (pack.size >= 2) =>
                    pack.last
                }
              }
              p.println(
                line match {
                  case pluginRegex(head, func, arg1, arg2, arg3) if (func.contains("paragraph")) =>
                    s"""${head}wiki.addParagraphPlugin(${arg1}, ${wikiPlugin(arg2)})"""
                  case pluginRegex(head, func, arg1, arg2, arg3) if (func.contains("inline")) =>
                    s"""${head}wiki.addInlinePlugin   (${arg1}, ${wikiPlugin(arg2)})"""
                  case _ =>
                    perlSyntaxToScala(line)
                }
              )
            case "}" if (className == "Install") =>
              p.println("}")
            case "}" =>
              if (beginClassParen) {
                p.println("")
                beginClassParen = false
              } else {
                p.println("}")
              }
            case "1;" =>
              p.println("}")
            /**
              * Process Perl statement defined lines
              */
            case _ =>
              p.println(perlSyntaxToScala(line))
          }
        }
      }
    }
  }
}
