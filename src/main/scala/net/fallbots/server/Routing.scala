package net.fallbots.server

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.{Http, server}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import net.fallbots.server.Routing.GetWebsocketFlow

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Routing {
  case object GetWebsocketFlow
}

class Routing(val actorSystem: ActorSystem) {

  implicit val as: ActorSystem      = actorSystem
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  def startServer(httpPort: Int): Unit = {
    Http()
      .newServerAt("0.0.0.0", httpPort)
      .bindFlow(gameServerRouting)
      .onComplete {
        case Success(value) => println(value)
        case Failure(err)   => println(err)
      }

  }

  val gameServerRouting: server.Route =
    pathEndOrSingleSlash {
      complete("WS server is alive\n")
    } ~ path("connect") {

      // expose the path /connect

      // create a client handler actor
      val handler    = as.actorOf(Props[ClientHandlerActor])
      val futureFlow = (handler ? GetWebsocketFlow)(3.seconds).mapTo[Flow[Message, Message, _]]

      onComplete(futureFlow) {
        case Success(flow) => handleWebSocketMessages(flow)
        case Failure(err)  => complete(err.toString)
      }
    }
}
