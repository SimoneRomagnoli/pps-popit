import sbtassembly.AssemblyPlugin.assemblySettings

assemblySettings

name := "pps-popit"

version := "0.1"

scalaVersion := "2.13.6"

val akkaVersion = "2.6.16"

resolvers in ThisBuild += Resolver.jcenterRepo

assemblyMergeStrategy in assembly := {
  case "reference.conf"            => MergeStrategy.concat
  case PathList("META-INF", _ @_*) => MergeStrategy.discard
  case _                           => MergeStrategy.first
}

lazy val javaFXModules = {
  // Determine OS version of JavaFX binaries
  lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux")   => "linux"
    case n if n.startsWith("Mac")     => "mac"
    case n if n.startsWith("Windows") => "win"
    case _ =>
      throw new Exception("Unknown platform!")
  }
  // Create dependencies for JavaFX modules
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
    .map(m => "org.openjfx" % s"javafx-$m" % "15.0.1" classifier osName)
}

libraryDependencies ++= javaFXModules

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "it.unibo.alice.tuprolog" % "2p-core" % "4.1.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalafx" %% "scalafx" % "15.0.1-R21",
  "com.novocode" % "junit-interface" % "0.11" % Test, // sbt's test interface for JUnit 4
  "org.junit.jupiter" % "junit-jupiter" % "5.7.1" % Test, // aggregator of junit-jupiter-api and junit-jupiter-engine (runtime)
  "org.junit.jupiter" % "junit-jupiter-engine" % "5.7.1" % Test, // for org.junit.platform
  "net.aichler" % "jupiter-interface" % "0.8.4" % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "org.junit.vintage" % "junit-vintage-engine" % "5.7.1" % Test,
  "org.junit.platform" % "junit-platform-launcher" % "1.7.1" % Test
)
