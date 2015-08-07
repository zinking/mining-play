name := """mining-play"""

version := "1.0-SNAPSHOT"


organization := "org.ms"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

//please run publish-local on mining first
libraryDependencies += "org.ms" %% "mining" % "0.0.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3",
  "junit" % "junit" % "4.10" % "test",
  "commons-io" % "commons-io" % "2.2",
  "ws.securesocial" %% "securesocial" % "master-SNAPSHOT" ,
  "org.scalatest" %% "scalatest" % "2.2.2" % "test",
  "org.mockito" % "mockito-all" % "1.8.4",
  "org.scalacheck" % "scalacheck_2.11" % "1.11.0"
)

libraryDependencies ++= Seq("com.typesafe.slick" %% "slick" % "2.0.1",
                            "com.h2database"     %  "h2"    % "1.3.166") 
