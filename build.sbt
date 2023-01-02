version := "0.1"

val AkkaVersion                = "2.7.0"
val AkkaHttpVersion            = "10.4.0"
val jakartaWebsocketApiVersion = "2.1.0"
val jettyWebsocketVersion      = "10.0.13"
val ScalaTestVersion           = "3.2.14"
enablePlugins(JavaAppPackaging)

ThisBuild / organization := "net.fallbots"
ThisBuild / scalaVersion := "2.13.10"

scalacOptions := Seq("-unchecked", "-deprecation")

lazy val shared = (project in file("shared")).settings(
  name := "fallbots-shared",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "upickle" % "2.0.0",

    // logging
    "ch.qos.logback" % "logback-classic" % "1.4.5",

    // testing
    "org.scalatest" %% "scalatest" % ScalaTestVersion % Test
  )
)

lazy val client = (project in file("client"))
  .dependsOn(shared)
  .settings(
    name := "fallbots-client",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http"        % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"      % AkkaVersion,

      // testing
      "org.scalatest" %% "scalatest" % ScalaTestVersion % Test
    )
  )

lazy val server = (project in file("server"))
  .dependsOn(shared)
  .settings(
    name := "fallbots-server",
    libraryDependencies ++= Seq(
      // for json message encoding/decoding

      "com.github.scopt" %% "scopt" % "4.1.0",

      // akka http
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream"      % AkkaVersion,
      "com.typesafe.akka" %% "akka-http"        % AkkaHttpVersion,

      // jetty
      "jakarta.websocket"           % "jakarta.websocket-api"  % jakartaWebsocketApiVersion,
      "org.eclipse.jetty.websocket" % "websocket-javax-server" % jettyWebsocketVersion,

      // testing
      "org.scalatest"     %% "scalatest"           % ScalaTestVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion      % Test,
      "com.typesafe.akka" %% "akka-http-testkit"   % AkkaHttpVersion  % Test
    )
  )

lazy val testing = (project in file("testing"))
  .dependsOn(client, server)
  .settings(
    name := "fallbots-test"
  )
