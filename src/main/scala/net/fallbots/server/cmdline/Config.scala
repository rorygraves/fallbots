package net.fallbots.server.cmdline

case class Config(
    port: Int = -1,
    serverImpl: ServerImpl.Impl = ServerImpl.Jetty
)
