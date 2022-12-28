package net.fallbots.server

import akka.actor.{Actor, ActorRef, Props}
import net.fallbots.game.basicgame.BoardPrinter
import net.fallbots.game.{Game, GameDef, GameRoundResult}
import net.fallbots.game.state.{Board, BotAction}
import net.fallbots.message.GameMessage
import net.fallbots.message.GameMessage.GameOver
import net.fallbots.server.GameActor.{BotMoveResponse, GameAssigned}
import net.fallbots.shared.BotId
import org.slf4j.LoggerFactory

import scala.util.Random

object GameActor {
  case class GameAssigned(gameId: String, gameRef: ActorRef)

  case class BotMoveResponse(botAction: BotAction)
  def props(gameId: String, gameDef: GameDef, botRefs: Map[BotId, ActorRef]): Props = Props(
    new GameActor(gameId, gameDef, botRefs)
  )
}

class GameActor(gameId: String, gameDef: GameDef, botRefs: Map[BotId, ActorRef]) extends Actor {

  private val random              = new Random()
  private val logger              = LoggerFactory.getLogger(s"GameActor:$gameId")
  private val refToBotLookup      = botRefs.toList.map(v => v._2 -> v._1).toMap
  private var _game: Option[Game] = None
  def game                        = _game.getOrElse(throw new IllegalStateException("game not initialised"))

  startGame()

  def startGame(): Unit = {
    botRefs.values.foreach { ref =>
      ref ! GameAssigned(gameId, self)
    }

    val (game, initialStates) = gameDef.createGame(random, botRefs.keys.toList)
    this._game = Some(game)
    BoardPrinter.printBoard(game.currentBoard)
    sendGameStatesToBots(initialStates)
  }

  var roundMoves: Map[BotId, BotAction] = Map.empty
  override def receive: Receive = {
    case BotMoveResponse(botAction: BotAction) =>
      refToBotLookup.get(sender()) match {
        case Some(botId) =>
          roundMoves = roundMoves + (botId -> botAction)
          if (roundMoves.size == botRefs.size) {
            applyRound()
          }
        case None =>
          logger.error(s"Illegal state - got message from ${sender()} and cannot map to bot")
      }
    case m =>
      logger.error("Boom ! " + m)
  }

  def applyRound(): Unit = {
    logger.info("Applying round")
    game.applyRound(random, roundMoves) match {
      case GameRoundResult.GameOver(winnerOpt) =>
        logger.info("GAME OVER  - notify player")
        val winnerMsg = GameOver(winnerOpt.map(_.id))
        botRefs.values.foreach { _ ! winnerMsg }
        context.stop(self)
      case GameRoundResult.GameRound(newStates) =>
        roundMoves = Map.empty
        sendGameStatesToBots(newStates)
    }
    BoardPrinter.printBoard(game.currentBoard)
  }

  def sendGameStatesToBots(states: Map[BotId, Board]): Unit = {
    states.foreach { case (botId, board) =>
      botRefs.get(botId) match {
        case Some(ref) =>
          ref ! GameMessage.GameMoveRequest(board)
        case None =>
          logger.error(s"Game state send to $botId - bot ref not found")
      }
    }
  }
}
