package net.fallbots.game

import net.fallbots.game.state.Board
import net.fallbots.shared.BotId

sealed trait GameRoundResult {}

object GameRoundResult {

  /** The game is over, give the winner */
  case class GameOver(winner: Option[BotId]) extends GameRoundResult

  /** The game is incomplete - return the next game state, and the bot information to pass to the players */
  case class GameRound(botStates: Map[BotId, Board]) extends GameRoundResult
}
