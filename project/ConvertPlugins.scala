import java.io.File
import sbt.Keys._
import sbt._

object ConvertPlugins extends CommonTrait {

  def remove() {
    val pluginDir = new File("./public/plugin/")
    val fi: Seq[File] = (pluginDir ** "*.pm").get

    fi foreach { x =>
      // /public/plugin/{name}/***.pm -> /app/plugin/{name}/***.scala
      val out = x.getPath.replace("./public/plugin/", "./app/plugin/").replace(".pm", ".scala")
      println("Plugin file " + x.getPath + " => " + out)
      val fo: File = new File(out)
      if (fo.exists) fo.delete
    }
  }

  def convert() {
    val pluginDir = new File("./public/plugin/")
    val fi: Seq[File] = (pluginDir ** "*.pm").get

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

      // load file contents
      printToFile(fo) { p =>
        scala.io.Source.fromFile(x).getLines.toList.map { line =>
          line match {
            case line if (line.startsWith("#") && line.matches("""^[#]*$""") && line != "#") =>
              p.println(line.replaceAll("#", "/"))
            case line if (line.startsWith("#")) =>
              p.println(line.replaceFirst("#", "//"))
            case line if (line.startsWith("use") && line.endsWith(";")) =>
              p.println("")
            case line if (line.startsWith("package") && line.endsWith(";")) =>
              p.println("package jp.gr.java_conf.hangedman." 
                + line.replaceAll("::", ".")
                .replace("package ", "")
                .replace(";", "")
              )
            case line if (line.startsWith("sub") && line.contains("new")) =>
              p.println(line.replace("sub", "class").replace("new", className))
            case line if (line.startsWith("sub")) =>
              if (line.contains("install")) {
                p.println(
                  line.replace("sub", s"object ${className} {\n")
                    .replace("install", s"def install(wiki: ${wikiPackage}.AbstractWiki)")
                )
              } else {
                p.println(line.replace("sub", "def"))
              }
            case line if (line.contains("$wiki") && line.contains("shift")) =>
              // do nothing
            case line if (line.contains("$wiki->")) =>
              p.println(
                line.replace("$wiki->", "wiki.")
                  .replace("add_paragraph_plugin", "addParagraphPlugin")
              )
            case "}" if (className == "Install") =>
              p.println("}")
            case "}" =>
              // do nothing
            case "1;" =>
              p.println("}")
            case _ =>
              p.println(line.replaceAll("#", "//"))
          }
        }
      }
    }
  }
}
