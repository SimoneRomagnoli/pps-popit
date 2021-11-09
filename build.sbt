import sbtassembly.AssemblyPlugin.assemblySettings

assemblySettings

name := "pps-popit"

version := "0.1"

scalaVersion := "2.13.6"

val akkaVersion = "2.6.16"
val circeVersion = "0.14.1"

resolvers += Resolver.jcenterRepo

assembly / assemblyMergeStrategy := {
  case "reference.conf"            => MergeStrategy.concat
  case PathList("META-INF", _ @_*) => MergeStrategy.discard
  case _                           => MergeStrategy.first
}

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8",
  "-feature",
  "-Ymacro-annotations"
)

lazy val javaFxLibrary = for {
  module <- Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
  os <- Seq("win", "mac", "linux")
} yield "org.openjfx" % s"javafx-$module" % "15.0.1" classifier os

libraryDependencies ++= javaFxLibrary

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "it.unibo.alice.tuprolog" % "2p-core" % "4.1.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalafx" %% "scalafx" % "15.0.1-R21",
  "org.scalafx" %% "scalafxml-core-sfx8" % "0.5",
  "org.typelevel" %% "cats-core" % "2.3.0",
  "org.typelevel" %% "cats-effect" % "2.3.0",
  "com.novocode" % "junit-interface" % "0.11" % Test, // sbt's test interface for JUnit 4
  "org.junit.jupiter" % "junit-jupiter" % "5.7.1" % Test, // aggregator of junit-jupiter-api and junit-jupiter-engine (runtime)
  "org.junit.jupiter" % "junit-jupiter-engine" % "5.7.1" % Test, // for org.junit.platform
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "org.junit.vintage" % "junit-vintage-engine" % "5.7.1" % Test,
  "org.junit.platform" % "junit-platform-launcher" % "1.7.1" % Test,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)
