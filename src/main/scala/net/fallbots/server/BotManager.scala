package net.fallbots.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import net.fallbots.server.BotManager.{
  BMBotRegistration,
  BMBotRegistrationResponse,
  RRAccepted,
  RRAlreadyConnected,
  RRRejected
}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

object BotManager {

  def props(gameManager: ActorRef): Props = Props(new BotManager(gameManager))

  case class BMBotRegistration(botId: Int, secret: String)

  trait RegResponse
  case object RRAccepted         extends RegResponse
  case object RRRejected         extends RegResponse
  case object RRAlreadyConnected extends RegResponse

  case class BMBotRegistrationResponse(response: RegResponse)

}

/** The BotManager is responsible for manging all of the connected bots.
  */
class BotManager(gameManager: ActorRef) extends Actor {

  val logger: Logger                = LoggerFactory.getLogger("BotManager")
  implicit val as: ActorSystem      = context.system
  implicit val ec: ExecutionContext = context.system.dispatcher

  case class BotState(botId: Int, botRef: ActorRef, activeGame: Option[ActorRef])

  var connectedBots: Map[Int, BotState] = Map[Int, BotState]()

  def checkSecret(botId: Int, secret: String): Boolean = {
    // TODO - handle secrets properly
    secret == "abc"
  }

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
      if (connectedBots.contains(botId))
        sender ! BMBotRegistrationResponse(RRAlreadyConnected)
      else if (!checkSecret(botId, secret))
        sender ! BMBotRegistrationResponse(RRRejected)
      else {
        sender ! BMBotRegistrationResponse(RRAccepted)
        connectedBots += botId -> BotState(botId, sender, None)
        gameManager ! GameManager.NewBotConnected(botId, sender)
        context.watch(sender)
      }
  }
}
