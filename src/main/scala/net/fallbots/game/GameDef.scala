package net.fallbots.game

import net.fallbots.game.state.Board
import net.fallbots.shared.BotId

import scala.util.Random

trait GameDef {

  /** Given a set of players, returning the initial game state and initial bot states.
    * @param random
    *   The randomness source
    * @param playerList
    *   The list of players participating in this game
    * @return
    *   A tuple of the initial game state and a map of bots and their initial state info
    */
  def createGame(random: Random, playerList: List[BotId]): (Game, Map[BotId, Board])
}
