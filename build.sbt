version := "0.1"

val AkkaVersion     = "2.7.0"
val AkkaHttpVersion = "10.4.0"

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "net.fallbots",
      scalaVersion := "2.13.8"
    )
  ),
  name := "fallbots",
  libraryDependencies ++= Seq(
    // for json message encoding/decoding
    "com.lihaoyi"      %% "upickle" % "2.0.0",
    "com.github.scopt" %% "scopt"   % "4.1.0",

    // akka http
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream"      % AkkaVersion,
    "com.typesafe.akka" %% "akka-http"        % AkkaHttpVersion,

    // logging
    "ch.qos.logback" % "logback-classic" % "1.4.4",

    // testing
    "org.scalatest"     %% "scalatest"           % "3.2.14"        % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion     % Test,
    "com.typesafe.akka" %% "akka-http-testkit"   % AkkaHttpVersion % Test
  )
)