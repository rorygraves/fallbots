package net.fallbots.server

import akka.actor.{Actor, ActorRef, PoisonPill, Timers}
import net.fallbots.message._
import net.fallbots.server.ClientHandlerActor.PingTimerNotify
import net.fallbots.shared.BotId
import org.slf4j.LoggerFactory

import scala.concurrent.duration.DurationInt

object ClientHandlerActor {
  case object PingTimerNotify
}
// The shared client messaging logic between different server implementations.
// specific servers (e.g. AkkaHttp provide override this implementing receive to be
// their initialisation logic.
abstract class ClientHandlerActor(botManager: ActorRef, gameManager: ActorRef) extends Actor with Timers {

  var _id: Option[BotId] = None
  def id                 = _id.getOrElse(throw new IllegalStateException("botId not set"))
  val logger             = LoggerFactory.getLogger(this.getClass.getName)
  def send(msg: FBMessage): Unit

  def mainReceive: Receive = registrationReceive

  def checkSecret(botId: Int, secret: String): Boolean = {
    // TODO - handle secrets properly
    secret == "abc"
  }

  private def registrationReceive: Receive = {
    case RegisterMessage(rawId, secret) =>
      _id = Some(BotId(rawId))
      botManager ! BotManager.BMBotRegistration(id, secret)
    case BotManager.BMBotRegistrationResponse(status) =>
      val connected = status match {
        case BotManager.RRAccepted =>
          send(RegistrationResponse(accepted = true, ""))
          true
        case BotManager.RRRejected =>
          send(RegistrationResponse(accepted = false, "Secret rejected"))
          false
        case BotManager.RRAlreadyConnected =>
          send(RegistrationResponse(accepted = false, "Already connected"))
          false
      }
      if (connected) {
        logger.info("Connected moving to connectedReceive")
        timers.startTimerAtFixedRate(PingTimerNotify, PingTimerNotify, 5.second)
        context.become(connectedReceive)
      } else {
        logger.warn("Connected rejected, terminating")
        self ! PoisonPill
      }
    case m =>
      logger.warn("Unhandled message in registrationReceive" + m)
  }

  // if set, this is the current game we are connected to
  private var activeGame: Option[ActorRef] = None

  def sendToGame(response: GameActor.BotMoveResponse): Unit = {
    activeGame match {
      case Some(gameRef: ActorRef) =>
        gameRef ! response
      case None =>
        logger.error("Unhandled - no game: " + response)
    }
  }

  private def connectedReceive: Receive = {
    case PingTimerNotify =>
      send(ServerPing)
    case ClientPong =>

    case GameMessage.FBRequestGame =>
      gameManager ! GameManager.RequestGame(id, self)
    case GameMessage.GameMoveResponse(round, action) =>
      sendToGame(GameActor.BotMoveResponse(round, action))

    case GameActor.BotMoveRequest(round, board) =>
      send(GameMessage.GameMoveRequest(round, board))
    case _: FBClientGameMessage =>
      logger.error("Unhandled game message")
    case m: FBClientMessage =>
      logger.info("Unhandled - client message: " + m)
    case GameManager.AwaitingGame =>
      send(GameMessage.AwaitingGame)
    case GameActor.GameAssigned(gameId, gameRef) =>
      activeGame = Some(gameRef)
      send(GameAssigned(gameId))
    case m: FBServerMessage =>
      send(m.asInstanceOf[FBMessage])
    case m =>
      logger.warn("Unhandled message in connectedReceive" + m)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    logger.warn("In preRestart:" + message, reason)
    super.preRestart(reason, message)
  }

  override def unhandled(message: Any): Unit = {
    logger.error("In unhandled: " + message)
  }

  // force implementor to implement postStop behaviour to cleanup
  override final def postStop(): Unit = {
    shutdown()
  }

  def shutdown(): Unit
}
