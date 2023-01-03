package net.fallbots.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import net.fallbots.server.BotManager._
import net.fallbots.server.auth.AuthService
import net.fallbots.shared.BotId
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

object BotManager {

  def props(gameManager: ActorRef, authService: AuthService): Props = Props(new BotManager(gameManager, authService))

  case class BMBotRegistration(botId: BotId, secret: String)

  trait RegResponse
  case object RRAccepted         extends RegResponse
  case object RRRejected         extends RegResponse
  case object RRAlreadyConnected extends RegResponse

  case class BMBotRegistrationResponse(response: RegResponse)

}

/** The BotManager is responsible for manging all of the connected bots.
  */
class BotManager(gameManager: ActorRef, authService: AuthService) extends Actor {

  val logger: Logger                = LoggerFactory.getLogger("BotManager")
  implicit val as: ActorSystem      = context.system
  implicit val ec: ExecutionContext = context.system.dispatcher

  case class BotState(botId: BotId, botRef: ActorRef, activeGame: Option[ActorRef])

  private var connectedBots = Map[BotId, BotState]()

  private def checkSecret(botId: BotId, secret: String): Boolean = authService.authorise(botId, secret)

  override def receive: Receive = {
    case Terminated(ref) =>
      connectedBots.values.find(bs => bs.botRef == ref) match {
        case Some(botState) =>
          logger.info(s"${botState.botId} - terminated removing from active pool")
          connectedBots -= botState.botId
          gameManager ! GameManager.BotDisconnected(botState.botId, botState.botRef)
        case None =>
          logger.warn("Got Terminated message for unknown reference " + ref)

      }
    case BMBotRegistration(botId, secret) =>
      val botActor = sender()
      if (connectedBots.contains(botId))
        botActor ! BMBotRegistrationResponse(RRAlreadyConnected)
      else if (!checkSecret(botId, secret))
        botActor ! BMBotRegistrationResponse(RRRejected)
      else {
        botActor ! BMBotRegistrationResponse(RRAccepted)
        connectedBots += botId -> BotState(botId, botActor, None)
        context.watch(botActor)
      }
  }
}
