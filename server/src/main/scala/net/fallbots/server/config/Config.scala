package net.fallbots.server.config

import net.fallbots.server.cmdline.ServerImpl

object Config {

  val DefaultMaxTimePerRoundMs = 2000
  val DefaultMaxRoundsPerGame  = 200

  val DefaultNoPlayers                   = 10
  val DefaultMinPlayersPerGame           = 2
  val DefaultMaxPlayersPerGame           = 4
  val DefaultMaxWaitForPlayersMs         = 2000
  val DefaultGameRandomSeed: Option[Int] = None

  val default: Config = Config()

  case class GameServerConfig(
      noPlayers: Int = DefaultNoPlayers,
      // The minimum number of players per game
      minPlayersPerGame: Int = DefaultMinPlayersPerGame,
      // the maximum number of players per game
      maxPlayersPerGame: Int = DefaultMaxPlayersPerGame,
      // the maximum wait (in ms) after minPlayers has been reached before the game is started
      maxWaitForPlayersMs: Int = DefaultMaxWaitForPlayersMs
  ) {}

  case class GameConfig(
      maxTimePerRoundMs: Int = DefaultMaxTimePerRoundMs,
      maxRoundsPerGame: Int = DefaultMaxRoundsPerGame,
      // The random seed
      gameRandomSeed: Option[Int] = DefaultGameRandomSeed
  )

  case class Config(
      port: Int = -1,
      serverImpl: ServerImpl.Impl = ServerImpl.Jetty,
      gameServerConfig: GameServerConfig = GameServerConfig(),
      gameConfig: GameConfig = GameConfig()
  )

}
