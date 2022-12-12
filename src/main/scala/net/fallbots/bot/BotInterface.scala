package net.fallbots.bot

import net.fallbots.game.state.{Board, BotAction}

trait BotInterface {

  def connected(): Unit

  def gameStarted(gameId: String): Unit

  def getMove(state: Board): BotAction

  def gameEnded(): Unit

}
