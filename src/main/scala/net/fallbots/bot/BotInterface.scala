package net.fallbots.bot

trait BotInterface {

  def connected(): Unit

  def gameStarted(gameId: String): Unit

  def getMove(state: String): String

  def gameEnded(): Unit

}
