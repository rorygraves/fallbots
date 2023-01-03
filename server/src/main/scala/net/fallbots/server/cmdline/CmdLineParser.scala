package net.fallbots.server.cmdline

import net.fallbots.server.config.Config
import scopt.OParser

object CmdLineParser {

  implicit val serverImplRead: scopt.Read[ServerImpl.Value] =
    scopt.Read.reads(ServerImpl.withName)

  private val builder = OParser.builder[Config.Config]
  private val argsParser = {
    import builder._
    OParser.sequence(
      programName("FallBots"),
      head("fallbots", "0.1"),

      // game server configuration
      opt[Int]("noPlayers")
        .action((x, c) => c.copy(gameServerConfig = c.gameServerConfig.copy(noPlayers = x)))
        .validate(v => if (v < 1) Left("must be >=1") else Right(()))
        .text(
          s"The total number of bots the server will accept - Default ${Config.DefaultNoPlayers})"
        ),

      // game configuration
      opt[Int]("maxTimePerRoundMs")
        .action((x, c) => c.copy(gameConfig = c.gameConfig.copy(maxTimePerRoundMs = x)))
        .validate(v => if (v < 100) Left("must be >=100") else Right(()))
        .text(
          s"The maximum number of milliseconds per round (bots taking too long will be ignored - Default ${Config.DefaultMaxTimePerRoundMs})"
        ),
      opt[Int]("maxRoundsPerGame")
        .action((x, c) => c.copy(gameConfig = c.gameConfig.copy(maxRoundsPerGame = x)))
        .text(s"The maximum number of rounds a game can last - Default ${Config.DefaultMaxRoundsPerGame}")
        .validate(v => if (v < 1) Left("must be >=1") else Right(())),
      opt[Int]("gameRandomSeed")
        .hidden()
        .action((x, c) => c.copy(gameConfig = c.gameConfig.copy(gameRandomSeed = Some(x))))
        .text(s"For testing - if given each game will start with this random seed"),

      // server configuration
      opt[Int]('p', "port")
        .required()
        .action((x, c) => c.copy(port = x))
        .text("The port to run the server http endpoint on"),
      opt[ServerImpl.Impl]('s', "serverImpl")
        .action((x, c) => c.copy(serverImpl = x))
        .text("Select the server websocket implementation (AkkaHttp, Jetty)")
    )
  }

  def parseCommandLine(args: Array[String]): Option[Config.Config] = {
    OParser.parse(argsParser, args, Config.default)
  }
}
