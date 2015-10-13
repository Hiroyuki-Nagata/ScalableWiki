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
    //resolvers += SonatypeReleases,
    libraryDependencies ++= LibraryDependencies
  )

  lazy val project = Project(
    "ScalableWiki",
    file(".")
  ).enablePlugins(play.PlayScala).settings(
    projectSettings: _*
  )
}

/**
val originalJvmOptions = sys.process.javaVmArguments.filter(
  a => Seq("-Xmx", "-Xms", "-XX").exists(a.startsWith)
)

val baseSettings = Seq(
  scalaVersion := "2.11.7",
  scalacOptions ++= (
    "-deprecation" ::
    "-unchecked" ::
    "-Xlint" ::
    "-language:existentials" ::
    "-language:higherKinds" ::
    "-language:implicitConversions" ::
    Nil
  ),
  watchSources ~= { _.filterNot(f => f.getName.endsWith(".swp") || f.getName.endsWith(".swo") || f.isDirectory) },
  javaOptions ++= originalJvmOptions,
  ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
  shellPrompt := { state =>
    val branch = if(file(".git").exists){
      "git branch".lines_!.find{_.head == '*'}.map{_.drop(1)}.getOrElse("")
    }else ""
    Project.extract(state).currentRef.project + branch + " > "
  },
  resolvers ++= Seq(Opts.resolver.sonatypeReleases)
)

lazy val root = Project(
  "ScalableWiki", file(".")
).enablePlugins(play.PlayScala).settings(
  baseSettings: _*
).settings(
  libraryDependencies ++= (
    Nil
  )
)

*/
