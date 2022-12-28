package net.fallbots.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import net.fallbots.game.basicgame.ExampleGame
import net.fallbots.message.GameMessage
import net.fallbots.server.GameManager.{BotDisconnected, RequestGame}
import net.fallbots.shared.BotId
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext
import scala.util.Random

object GameManager {

  def props(minPlayersPerGame: Int): Props = Props(new GameManager(minPlayersPerGame))

  final case class RequestGame(botId: BotId, botRef: ActorRef)
  final case class BotDisconnected(botId: BotId, botRef: ActorRef)

  case object AwaitingGame
}

/** The GameManger is responsible for game lifecycles.
  */
class GameManager(minPlayersPerGame: Int) extends Actor {
  val logger: Logger                = LoggerFactory.getLogger("GameManager")
  implicit val as: ActorSystem      = context.system
  implicit val ec: ExecutionContext = context.system.dispatcher

  var nextGameId = 1

  var activeGames: Map[String, ActorRef] = Map.empty

  var waiting: List[(BotId, ActorRef)] = List.empty

  val random = new Random(1)

  def handleGameRequest(botId: BotId, ref: ActorRef): Unit = {
    logger.info(s"Handling game request for $botId")
    ref ! GameMessage.AwaitingGame
    waiting = waiting ::: List((botId, ref))
    assignGames()
  }

  def assignGames(): Unit = {
    if (waiting.size >= minPlayersPerGame) {
      val players = waiting.take(minPlayersPerGame)
      waiting = waiting.drop(minPlayersPerGame)

      val gameId = nextGameId.toString
      nextGameId = nextGameId + 1

      val gameRef = context.actorOf(GameActor.props(gameId, ExampleGame, players.toMap, 2000))
      activeGames = activeGames + (gameId -> gameRef)
    }
  }
  override def receive: Receive = {
    case RequestGame(botId, ref: ActorRef) =>
      logger.info(s"Bot $botId requested game")
      handleGameRequest(botId, ref)
    case BotDisconnected(botId, ref: ActorRef) =>
      if (waiting.exists(_._1 == botId)) {
        waiting = waiting.filter(_._1 != botId)
        logger.info(s"Bot disconnected  $botId")
      }
    case m =>
      logger.warn("Unknown message to game manager - ignored:" + m)
  }
}
