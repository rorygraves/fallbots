version := "0.1"

val AkkaVersion                = "2.7.0"
val AkkaHttpVersion            = "10.4.0"
val jakartaWebsocketApiVersion = "2.1.0"
val jettyWebsocketVersion      = "10.0.13"

enablePlugins(JavaAppPackaging)

ThisBuild / organization := "net.fallbots"
ThisBuild / scalaVersion := "2.13.10"

scalacOptions := Seq("-unchecked", "-deprecation")

lazy val root = (project in file(".")).settings(
  name := "fallbots",
  libraryDependencies ++= Seq(
    // for json message encoding/decoding
    "com.lihaoyi"      %% "upickle" % "2.0.0",
    "com.github.scopt" %% "scopt"   % "4.1.0",

    // akka http
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream"      % AkkaVersion,
    "com.typesafe.akka" %% "akka-http"        % AkkaHttpVersion,

    // jetty
    "jakarta.websocket"           % "jakarta.websocket-api"  % jakartaWebsocketApiVersion,
    "org.eclipse.jetty.websocket" % "websocket-javax-server" % jettyWebsocketVersion,

    // logging
    "ch.qos.logback" % "logback-classic" % "1.4.5",

    // testing
    "org.scalatest"     %% "scalatest"           % "3.2.14"        % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion     % Test,
    "com.typesafe.akka" %% "akka-http-testkit"   % AkkaHttpVersion % Test
  )
)
