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
      "org.alephium" % "alephium-protocol_2.13" % "1.5.0-rc4",
      "org.alephium" % "alephium-api_2.13" % "1.5.0-rc4",
      "com.lihaoyi" %% "pprint" % "0.7.0",
      "info.picocli" % "picocli" % "4.6.3"
    )
  )


assemblyMergeStrategy in assembly := {
  case "META-INF/versions/9/module-info.class" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
