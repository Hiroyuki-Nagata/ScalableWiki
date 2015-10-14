//import play.Project._
import sbt.Keys._
import sbt._

object ScalableWiki extends Build {

  val Organization  = "jp.gr.java_conf.hangedman"
  val Name          = "scalablewiki"
  val Version       = "0.0.1-SNAPSHOT" 
  val ScalaVersion  = "2.10.6"

  val originalJvmOptions = sys.process.javaVmArguments.filter(
    a => Seq("-Xmx", "-Xms", "-XX").exists(a.startsWith)
  )

  // << groupId >> %%  << artifactId >> % << version >>
  lazy val LibraryDependencies = Seq(
    "jp.t2v" %% "play2-auth"      % "0.13.5",
    "jp.t2v" %% "play2-auth-test" % "0.13.5" % "test"
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
    libraryDependencies ++= LibraryDependencies
  )

  lazy val project = Project(
    "ScalableWiki",
    file(".")
  ).enablePlugins(play.PlayScala).settings(
    projectSettings: _*
  )
}
