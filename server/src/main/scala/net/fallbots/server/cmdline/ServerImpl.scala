package net.fallbots.server.cmdline

object ServerImpl extends Enumeration {
  type Impl = Value
  val AkkaHttp, Jetty = Value
}

