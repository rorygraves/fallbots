package net.fallbots.game

import net.fallbots.game.state.{Board, BotAction}
import net.fallbots.shared.BotId

import scala.util.Random

/** An immutable class representing the current game state */
trait Game {
  def applyRound(random: Random, moves: Map[BotId, BotAction]): GameRoundResult
  def currentBoard: Board
}
