name := """heroeswm"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.4",
  "org.scalaj" % "scalaj-http_2.11" % "2.3.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
  "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2",
  "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.6.1",
  "org.reactivemongo" % "reactivemongo_2.11" % "0.11.12",
  "org.slf4j" % "slf4j-api" % "1.7.21"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
