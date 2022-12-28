package net.fallbots.server.jetty

import akka.actor.{ActorRef, ActorSystem}
import net.fallbots.message.FBMessage
import net.fallbots.server.jetty.JettyWebsocketHandler.{actorSystem, botManager, gameManager}

import javax.websocket._
import javax.websocket.server.ServerEndpoint
import scala.annotation.unused

object JettyWebsocketHandler {

  // jetty instantiates an instance for each connection so we need to fudge in access
  //  to the actorSystem and BotManager.

  private var _actorSystem: Option[ActorSystem] = None
  private var _botManager: Option[ActorRef]     = None
  private var _gameManager: Option[ActorRef]    = None

  def initialise(actorSystem: ActorSystem, botManager: ActorRef, gameManager: ActorRef): Unit = {
    _actorSystem = Some(actorSystem)
    _botManager = Some(botManager)
    _gameManager = Some(gameManager)
  }
  def actorSystem: ActorSystem =
    _actorSystem.getOrElse(throw new IllegalStateException("JettyWebsocketHandler.actorSystem not initialised"))

  def botManager: ActorRef =
    _botManager.getOrElse(throw new IllegalStateException("JettyWebsocketHandler.botManager not initialised"))

  def gameManager: ActorRef =
    _gameManager.getOrElse(throw new IllegalStateException("JettyWebsocketHandler.gameManger not initialised"))

}

@ClientEndpoint
@ServerEndpoint(value = "/connect")
class JettyWebsocketHandler {
  def send(msg: FBMessage): Unit = {
    import net.fallbots.message.MessageImplicits._
    import upickle.default._
    val messageTxt = write(msg)
    session.getBasicRemote.sendText(messageTxt)
  }

  private var _actor: Option[ActorRef] = None
  private def actor: ActorRef =
    _actor.getOrElse(throw new IllegalStateException("Actor not set, something has gone wrong"))

  private var _session: Option[Session] = None
  private def session: Session =
    _session.getOrElse(throw new IllegalStateException("session not set, something has gone wrong"))

  @OnOpen @unused def onWebSocketConnect(sess: Session): Unit = {
    _session = Some(sess)
    _actor = Some(actorSystem.actorOf(JettyClientWebsocketHandlerActor.props(botManager, gameManager, this)))
    System.out.println("Socket Connected: " + sess)
  }

  @OnMessage @unused
  def onWebSocketText(@unused sess: Session, message: String): Unit = {
    import net.fallbots.message.MessageImplicits._
    import upickle.default._
    val messageObj = read[net.fallbots.message.FBMessage](message)
    actor ! messageObj
  }

  @OnClose @unused def onWebSocketClose(reason: CloseReason): Unit = {
    System.out.println("Socket Closed: " + reason)
  }

  @OnError @unused def onWebSocketError(cause: Throwable): Unit = {
    cause.printStackTrace(System.err)
  }

  def close(): Unit = {
    session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Thanks"))
  }
}
