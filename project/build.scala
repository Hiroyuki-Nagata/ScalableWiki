import sbt.Keys._
import sbt._
import com.heroku.sbt.HerokuPlugin
import play.PlayScala

object ScalableWiki extends Build {

  val Organization  = "jp.gr.java_conf.hangedman"
  val Name          = "scalablewiki"
  val Version       = "0.0.1-SNAPSHOT" 
  val ScalaVersion  = "2.10.6"

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }

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

  lazy val tmplCommand =
    Command.command("gen-tmpl") { (state: State) =>

      import java.io.File

      println("Generating template files...")
      val tmplDir = new File("./public/tmpl/")
      val fi: Seq[File] = (tmplDir ** "*.tmpl").get

      fi foreach { x =>
        val out = x.getPath.replace("./public/tmpl/", "./app/views/") + ".html"
        println("Template file " + x.getPath + " => " + out)
        val fo: File = new File(out)
        fo.getParentFile.mkdirs
        fo.createNewFile
        // load file contents
        printToFile(fo) { p =>
          scala.io.Source.fromFile(x).getLines.toList.map { line =>

            (line.contains("TMPL_VAR NAME"),
              (line.contains("TMPL_IF") || line.contains("TMPL_UNLESS"))
            ) match {
              case (true, true) =>
                tmplVarAndIfElseToScala(line)
              case (true, false) =>
                tmplVarToScala(line)
              case (false, true) =>
                tmplIfElseToScala(line)
              case (false, false) =>
                line
            }
          }.foreach { line =>
            p.println(line)
          }
        }
      }
      state
    }

  val originalJvmOptions = sys.process.javaVmArguments.filter(
    a => Seq("-Xmx", "-Xms", "-XX").exists(a.startsWith)
  )

  // << groupId >> %%  << artifactId >> % << version >>
  lazy val LibraryDependencies = Seq(
    "jp.t2v" %% "play2-auth"      % "0.13.5",
    "jp.t2v" %% "play2-auth-test" % "0.13.5" % "test",
    "com.typesafe" % "config" % "1.3.0",
    "net.ceedubs" %% "ficus" % "1.0.1"
  )

  lazy val projectSettings = Seq(
    scalacOptions ++= (
      "-deprecation" ::
        "-unchecked" ::
        "-Xlint" ::
        "-language:existentials" ::
        "-language:higherKinds" ::
        "-language:implicitConversions" ::
        Nil
    ),
    watchSources ~= { 
      _.filterNot(f => f.getName.endsWith(".swp") || f.getName.endsWith(".swo") || f.isDirectory) 
    },
    javaOptions ++= originalJvmOptions,
    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
    shellPrompt := { state =>
      val branch = if(file(".git").exists){
        "git branch".lines_!.find{_.head == '*'}.map{_.drop(1)}.getOrElse("")
      }else ""
      Project.extract(state).currentRef.project + branch + " > "
    },
    parallelExecution in Test := false,
    testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console"),
    organization := Organization,
    name := Name,
    version := Version,
    scalaVersion := ScalaVersion,
    resolvers += DefaultMavenRepository,
    resolvers += Classpaths.typesafeReleases,
    libraryDependencies ++= LibraryDependencies,
    commands += tmplCommand,
    //
    // If you use original name,
    // $ sbt -DherokuAppName=myapp stage deployHeroku
    //
    HerokuPlugin.autoImport.herokuAppName in Compile := {
      if (sys.props("herokuAppName") == null) { 
        "scalable-wiki" 
      } else { 
        sys.props("herokuAppName")
      }
    }
  )

  lazy val project = Project(
    "ScalableWiki",
    file(".")
  ).enablePlugins(PlayScala, HerokuPlugin).settings(
    projectSettings: _*
  )
}
