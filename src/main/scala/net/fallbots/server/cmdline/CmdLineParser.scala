package net.fallbots.server.cmdline

import scopt.OParser

object CmdLineParser {
  val builder = OParser.builder[Config]
  val parser1 = {
    import builder._
    OParser.sequence(
      programName("FallBots"),
      head("fallbots", "0.1"),
      // option -f, --foo
      opt[Int]('p', "port")
        .required()
        .action((x, c) => c.copy(port = x))
        .text("The port to run the server http endpoint on")
    )
  }

  def parseCommandLine(args: Array[String]): Option[Config] = {
    OParser.parse(parser1, args, Config())
  }
}
