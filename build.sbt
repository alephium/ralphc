import Dependencies._
import sbt.Keys._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := Version.version
ThisBuild / organization     := "org.ralphc"
ThisBuild / organizationName := "ralphc"

resolvers += "Sonatype s01 Releases" at "https://s01.oss.sonatype.org/content/repositories/snapshots/"

lazy val root = (project in file("."))
  .settings(
    name := "ralphc",
    assembly / assemblyJarName := "ralphc.jar",
    libraryDependencies ++= Seq(
      utilCore,
      "org.alephium" % "alephium-protocol_2.13" % Version.version,
      "org.alephium" % "alephium-api_2.13" % Version.version,
      "org.alephium" % "alephium-ralph_2.13" % Version.version,
      "org.alephium" % "alephium-crypto_2.13" % Version.version,
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

enablePlugins(Antlr4Plugin)
antlr4PackageName in Antlr4 := Some("org.alephium.antlr4.ralph")
antlr4Version in Antlr4 := "4.10.1" // default: 4.8-1
antlr4GenListener in Antlr4 := true // default: true
antlr4GenVisitor in Antlr4 := true // default: false
antlr4TreatWarningsAsErrors in Antlr4 := true // default: false
