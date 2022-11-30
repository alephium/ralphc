import sbt._

object Dependencies {
  lazy val utilCore  = "com.twitter" %% "util-core" % "21.11.0"
  lazy val commonsCli  = "commons-cli" % "commons-cli" % "1.5.0"
}

object Version {
  lazy val version = "1.5.4"
}
