package net.fallbots.bot

import net.fallbots.game.state.{Board, BotAction}
import net.fallbots.shared.BotId

import scala.annotation.unused

class NoOpBot(@unused botId: BotId) extends BotInterface {
  override def connected(): Unit = {}

  override def gameStarted(gameId: String): Unit = {}

  override def getMove(state: Board): BotAction = {
    Thread.sleep(100000)
    BotAction.None
  }

  override def gameEnded(): Unit = {}
}
