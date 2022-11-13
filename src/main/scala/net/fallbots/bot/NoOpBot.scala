package net.fallbots.bot

class NoOpBot extends BotInterface {
  override def connected(): Unit = {}

  override def gameStarted(gameId: String): Unit = {}

  override def getMove(state: String): String = { "" }

  override def gameEnded(): Unit = {}
}
