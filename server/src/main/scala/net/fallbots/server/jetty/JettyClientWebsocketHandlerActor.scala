package net.fallbots.server.jetty

import akka.actor.{ActorRef, ActorSystem, Props}
import net.fallbots.message.FBMessage
import net.fallbots.server.{ClientHandlerActor, GameManager}

import scala.concurrent.ExecutionContext

object JettyClientWebsocketHandlerActor {
  def props(botManager: ActorRef, gameManager: ActorRef, wsHandler: JettyWebsocketHandler): Props = Props(
    new JettyClientWebsocketHandlerActor(botManager, gameManager, wsHandler)
  )
}

class JettyClientWebsocketHandlerActor(botManager: ActorRef, gameManager: ActorRef, wsHandler: JettyWebsocketHandler)
    extends ClientHandlerActor(botManager, gameManager) {

  implicit val as: ActorSystem      = context.system
  implicit val ec: ExecutionContext = context.system.dispatcher

  override def send(msg: FBMessage): Unit = wsHandler.send(msg)

  override def shutdown(): Unit = {
    wsHandler.close()
  }

  override def receive: Receive = mainReceive
}
