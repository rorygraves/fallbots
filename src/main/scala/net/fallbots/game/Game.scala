package net.fallbots.game

import net.fallbots.game.state.{Board, BotAction}
import net.fallbots.shared.BotId

/** An immutable class representing the current game state */
trait Game {
  def applyRound(moves: Map[BotId, BotAction]): GameRoundResult
  def currentBoard: Board
}
