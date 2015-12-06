name := """mining-play"""

version := "1.0-SNAPSHOT"


organization := "org.ms"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
    filters,
    cache,
    ws,
    specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator


libraryDependencies ++= Seq(
    "org.ms" %% "mining" % "0.0.1",
    "junit" % "junit" % "4.10" % "test",
    "commons-io" % "commons-io" % "2.2",
    "org.scalatest" %% "scalatest" % "2.2.2" % "test",
    "org.mockito" % "mockito-all" % "1.8.4",
    "org.apache.commons" % "commons-dbcp2" % "2.1.1",
    "mysql" % "mysql-connector-java" % "5.1.35",
    "io.spray" %%  "spray-json" % "1.3.2",
    "com.zaxxer" % "HikariCP" % "2.4.3",
    "org.scalacheck" % "scalacheck_2.11" % "1.11.0"
)


fork in run := true