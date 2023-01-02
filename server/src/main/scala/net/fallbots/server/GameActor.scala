package net.fallbots.server

import akka.actor.{Actor, ActorRef, Props, Timers}
import net.fallbots.game.basicgame.BoardPrinter
import net.fallbots.game.{Game, GameDef, GameRoundResult}
import net.fallbots.game.state.{Board, BotAction}
import net.fallbots.message.GameMessage.GameOver
import net.fallbots.server.GameActor.{BotMoveResponse, GameAssigned, RoundTimeout}
import net.fallbots.server.config.Config
import net.fallbots.shared.BotId
import org.slf4j.LoggerFactory

import scala.concurrent.duration.DurationInt
import scala.util.Random

object GameActor {
  // messages
  case class GameAssigned(gameId: String, gameRef: ActorRef)

  case class BotMoveRequest(round: Int, board: Board)
  case class BotMoveResponse(round: Int, botAction: BotAction)

  case class RoundTimeout(roundId: Int)

  def props(gameId: String, gameDef: GameDef, botRefs: Map[BotId, ActorRef], config: Config.GameConfig): Props = Props(
    new GameActor(gameId, gameDef, botRefs, config)
  )
}

class GameActor(gameId: String, gameDef: GameDef, botRefs: Map[BotId, ActorRef], config: Config.GameConfig)
    extends Actor
    with Timers {

  val randomSeed = config.gameRandomSeed.getOrElse(Random.nextInt())
  // initialise the rng to the fixed seed if given otherwise default to an standard random
  private val random = new Random(randomSeed)

  private val logger         = LoggerFactory.getLogger(s"GameActor:$gameId")
  private val refToBotLookup = botRefs.toList.map(v => v._2 -> v._1).toMap

  private var _game: Option[Game] = None
  private def game                = _game.getOrElse(throw new IllegalStateException("game not initialised"))

  private var roundMoves: Map[BotId, BotAction] = Map.empty

  startGame()

  def startGame(): Unit = {
    botRefs.values.foreach { ref =>
      ref ! GameAssigned(gameId, self)
    }

    logger.info(s"Creating game with random seed: $randomSeed")
    val (game, initialStates) = gameDef.createGame(random, botRefs.keys.toList, config.maxRoundsPerGame)
    this._game = Some(game)
    BoardPrinter.printBoard(game.currentBoard)
    sendGameStatesToBots(initialStates)
    scheduleRoundTimeout()
  }

  override def receive: Receive = {
    case RoundTimeout(roundId) =>
      println(s"RoundTimeout($roundId)")
      if (roundId == game.currentRound) {
        logger.info("Round timeout triggered - apply received moves")
        applyRound()
      }
    case BotMoveResponse(round, botAction: BotAction) =>
      refToBotLookup.get(sender()) match {
        case Some(botId) =>
          if (round != game.currentRound)
            logger.warn(s"Move for $botId rejected - wrong round")
          else {

            roundMoves = roundMoves + (botId -> botAction)
            if (roundMoves.size == botRefs.size) {
              applyRound()
            }
          }
        case None =>
          logger.error(s"Illegal state - got message from ${sender()} and cannot map to bot")
      }
    case m =>
      logger.error("Boom ! " + m)
  }

  def scheduleRoundTimeout(): Unit = {
    val roundTimeoutMsg = RoundTimeout(game.currentRound)
    timers.startSingleTimer(roundTimeoutMsg, roundTimeoutMsg, config.maxTimePerRoundMs.millis)
  }

  def applyRound(): Unit = {
    timers.cancel(RoundTimeout(game.currentRound))
    logger.info("Applying round")
    game.applyRound(random, roundMoves) match {
      case GameRoundResult.GameOver(winnerOpt) =>
        logger.info("GAME OVER  - notify player")
        val winnerMsg = GameOver(winnerOpt.map(_.id))
        botRefs.values.foreach { _ ! winnerMsg }
        context.stop(self)
      case GameRoundResult.GameRound(newStates) =>
        roundMoves = Map.empty
        scheduleRoundTimeout()
        sendGameStatesToBots(newStates)
    }
    BoardPrinter.printBoard(game.currentBoard)
  }

  def sendGameStatesToBots(states: Map[BotId, Board]): Unit = {
    states.foreach { case (botId, board) =>
      botRefs.get(botId) match {
        case Some(ref) =>
          ref ! GameActor.BotMoveRequest(game.currentRound, board)
        case None =>
          logger.error(s"Game state send to $botId - bot ref not found")
      }
    }
  }
}
