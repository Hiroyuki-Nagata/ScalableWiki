import java.io.File
import sbt.Keys._
import sbt._
import com.heroku.sbt.HerokuPlugin
import play.PlayScala

object ScalableWiki extends Build with HtmlTemplateConverter with CommonTrait {

  val Organization  = "jp.gr.java_conf.hangedman"
  val Name          = "scalablewiki"
  val Version       = "0.0.1-SNAPSHOT"
  val ScalaVersion  = "2.10.6"

  lazy val tmplCommand =
    Command.command("gen-tmpl") { (state) =>

      println("Generating template files...")
      val tmplDir = new File("./public/tmpl/")
      val fi: Seq[File] = (tmplDir ** "*.tmpl").get
      val gen = new GenerateCaseClasses
      val manyArgFileBuffer = scala.collection.mutable.ListBuffer.empty[String]

      fi foreach { x =>
        // /public/tmpl/***.tmpl -> /app/views/***.scala.html
        val out = x.getPath.replace("./public/tmpl/", "./app/views/").replace(".tmpl", ".scala.html")
        println("Template file " + x.getPath + " => " + out)
        val fo: File = new File(out)
        fo.getParentFile.mkdirs
        fo.createNewFile
        // load file contents
        printToFile(fo) { p =>
          scala.io.Source.fromFile(x).getLines.toList.map { line =>

            (line.contains("TMPL_VAR"),
              (line.contains("TMPL_IF") || line.contains("TMPL_UNLESS"))
            ) match {
              case (true, true) =>
                tmplVarAndIfElseToScala(line)
              case (true, false) =>
                tmplVarToScala(line)
              case (false, true) =>
                tmplIfElseToScala(line)
              case (false, false) =>
                line.replaceAll("<!--TMPL_ELSE-->", "} else {")
            }
          }.toList match {
            case lines: List[String] =>
              val formatted = tmplMultiLinesToScala(lines)
              val arguments = getScalaTemplateArguments(formatted)(gen)

              (List(arguments.mkString("@(", ", ", ")"), "") ++ formatted).foreach {
                line => p.println(line)
              }

              if (arguments.size > 20) {
                manyArgFileBuffer += out
              }
          }
        }
      }

      // If there are some sources have many arguments
      manyArgFileBuffer.foreach { x =>
        new ReduceArguments(x).reduce
      }

      state
    }


  lazy val genPluginCommand =
    Command.args("gen-plugins", "<plugin-name>") { (state, args) =>
      println("Convert Perl plugin files..." + args.mkString(", "))
      ConvertPlugins.convert(args)
      state
    }

  lazy val cleanPluginCommand =
    Command.command("clean-plugins") { (state) =>
      println("Clean Perl plugin files...")
      ConvertPlugins.clean
      state
    }

  val originalJvmOptions = sys.process.javaVmArguments.filter(
    a => Seq("-Xmx", "-Xms", "-XX").exists(a.startsWith)
  )

  // << groupId >> %%  << artifactId >> % << version >>
  lazy val LibraryDependencies = Seq(
    "com.google.guava" % "guava" % "18.0",
    "com.typesafe" % "config" % "1.3.0",
    "commons-io" % "commons-io" % "2.4",
    "javax.mail" % "mail" % "1.4.7",
    "jp.t2v" %% "play2-auth"      % "0.13.5",
    "jp.t2v" %% "play2-auth-test" % "0.13.5" % "test",
    "net.ceedubs" %% "ficus" % "1.0.1",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
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
    scalacOptions in (Compile,doc) := Seq("-groups", "-implicits", "-diagrams"),
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
    commands ++= Seq(tmplCommand, genPluginCommand, cleanPluginCommand),
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
