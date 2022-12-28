package net.fallbots.server.cmdline

import scopt.OParser

object CmdLineParser {

  implicit val serverImplRead: scopt.Read[ServerImpl.Value] =
    scopt.Read.reads(ServerImpl.withName)

  private val builder = OParser.builder[Config]
  private val argsParser = {
    import builder._
    OParser.sequence(
      programName("FallBots"),
      head("fallbots", "0.1"),
      // option -f, --foo
      opt[Int]('p', "port")
        .required()
        .action((x, c) => c.copy(port = x))
        .text("The port to run the server http endpoint on"),
      opt[ServerImpl.Impl]('s', "serverImpl")
        .action((x, c) => c.copy(serverImpl = x))
        .text("Select the server websocket implementation (AkkaHttp, Jetty)")
    )
  }

  def parseCommandLine(args: Array[String]): Option[Config] = {
    OParser.parse(argsParser, args, Config())
  }
}
