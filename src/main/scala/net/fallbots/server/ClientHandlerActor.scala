package net.fallbots.server

import akka.actor.{ActorRef, PoisonPill, Props}
import net.fallbots.message.{FBClientMessage, FBMessage, FBServerMessage, RegisterMessage, RegistrationResponse}
import net.fallbots.server.GameManager.AwaitingGame

object ClientHandlerActor {
  def props(botManager: ActorRef): Props = Props(new ClientHandlerActor(botManager))
}
class ClientHandlerActor(botManager: ActorRef) extends WebsocketHandlerActor {

  def mainReceive: Receive = registrationReceive

  def registrationReceive: Receive = {
    case RegisterMessage(id, secret) =>
      botManager ! BotManager.BMBotRegistration(id, secret)
    case BotManager.BMBotRegistrationResponse(status) =>
      val connected = status match {
        case BotManager.RRAccepted =>
          down ! RegistrationResponse(accepted = true, "")
          true
        case BotManager.RRRejected =>
          down ! RegistrationResponse(accepted = false, "Secret rejected")
          false
        case BotManager.RRAlreadyConnected =>
          down ! RegistrationResponse(accepted = false, "Already connected")
          false
      }
      if (connected)
        context.become(connectedReceive)
      else {
        println("Connected rejected, terminating")
        self ! PoisonPill
      }
    case m: FBMessage =>
      println("Got message: :" + m)

  }

  // if set, this is the current game we are connected to
  var activeGame: Option[ActorRef] = None

  def connectedReceive: Receive = {
    case m: FBClientMessage =>
      activeGame match {
        case Some(actorRef: ActorRef) =>
        case None                     =>
      }
    case GameManager.AwaitingGame =>
      down ! AwaitingGame
    case m: FBServerMessage =>
      down ! m
    case m =>
      println("Got message " + m)
  }

  override def postStop(): Unit = {
    println("POST STOP Client handler")
    down ! PoisonPill
  }
}
