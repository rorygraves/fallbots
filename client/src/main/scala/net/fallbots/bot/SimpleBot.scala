package net.fallbots.bot

import net.fallbots.game.state.Direction.{East, North, South, West}
import net.fallbots.game.state.{Board, BotAction}
import net.fallbots.shared.BotId

class SimpleBot(botId: BotId) extends BotInterface {
  override def connected(): Unit = {}

  override def gameStarted(gameId: String): Unit = {}

  override def getMove(state: Board): BotAction = {

    val currentPos = state.bots(botId)
    val targetPos  = state.target

    val targetDir =
      if (currentPos.x < targetPos.x) East
      else if (currentPos.x > targetPos.x) West
      else if (currentPos.y < targetPos.y) North
      else South

    val currentDir = state.squares(currentPos).bot.get.direction
    if (currentDir == targetDir)
      BotAction.Forward
    else
      BotAction.RotateLeft
  }

  override def gameEnded(): Unit = {}
}
