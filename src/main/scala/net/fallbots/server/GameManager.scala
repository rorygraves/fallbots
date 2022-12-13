package net.fallbots.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import net.fallbots.server.GameManager.{BotDisconnected, NewBotConnected}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

object GameManager {

  def props(maxGames: Int): Props = Props(new GameManager(maxGames))

  final case class NewBotConnected(botId: Int, botRef: ActorRef)

  case object AwaitingGame

  final case class BotDisconnected(botId: Int, botRef: ActorRef)
}

/** The GameManger is responsible for game lifecycles.
  */
class GameManager(maxGames: Int) extends Actor {
  val logger: Logger                = LoggerFactory.getLogger("GameManager")
  implicit val as: ActorSystem      = context.system
  implicit val ec: ExecutionContext = context.system.dispatcher

  class BotState(botId: Int, actorRef: ActorRef, gameId: Option[Int])
  class GameState(gameId: Int, players: Set[Int], maxPlayers: Int, gameRef: ActorRef)
  var activeGames: Map[Int, GameState] = Map.empty

  override def receive: Receive = {
    case NewBotConnected(botId, ref: ActorRef) =>
      logger.info(s"New bot connected $botId")
    case BotDisconnected(botId, ref: ActorRef) =>
      logger.info(s"Bot disconnected  $botId")
    case m =>
      logger.warn("Unknown message to game manager - ignored:" + m)
  }
}
