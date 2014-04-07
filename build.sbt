import play.Project._

name := "mining-play"

organization := "org.ms"

version := "1.0"

playScalaSettings

//please run publish-local on mining first
libraryDependencies += "org.ms" %% "mining" % "0.0.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3",
  "junit" % "junit" % "4.10" % "test",
//  "securesocial" %% "securesocial" % "2.1.1",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test" 
)


