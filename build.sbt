import play.Project._

name := "mining-play"

organization := "org.ms"

version := "1.0"

playScalaSettings

//please run publish-local on mining first
libraryDependencies += "org.ms" %% "mining" % "0.0.1"