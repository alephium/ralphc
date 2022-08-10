import Dependencies._
import sbt.Keys._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.0.1"
ThisBuild / organization     := "org.ralphc"
ThisBuild / organizationName := "ralphc"

resolvers += "Sonatype s01 Releases" at "https://s01.oss.sonatype.org/content/repositories/snapshots/"

lazy val root = (project in file("."))
  .settings(
    name := "ralphc",
    assembly / assemblyJarName := "ralphc.jar",
    libraryDependencies ++= Seq(
      utilCore,
      "com.lihaoyi" %% "pprint" % "0.7.0",
      "info.picocli" % "picocli" % "4.6.3"
    )
  )





