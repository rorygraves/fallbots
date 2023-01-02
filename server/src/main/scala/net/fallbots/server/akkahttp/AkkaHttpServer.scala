package net.fallbots.server.akkahttp

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.{Http, server}
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import net.fallbots.server.AbstractServer
import net.fallbots.server.akkahttp.AkkaHttpServer.GetWebsocketFlow

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object AkkaHttpServer {
  case object GetWebsocketFlow
}

class AkkaHttpServer(val actorSystem: ActorSystem, botManager: ActorRef, gameManager: ActorRef) extends AbstractServer {

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
      val handler    = as.actorOf(AkkaHttpClientWebsocketHandlerActor.props(botManager, gameManager))
      val futureFlow = (handler ? GetWebsocketFlow)(3.seconds).mapTo[Flow[Message, Message, _]]

      onComplete(futureFlow) {
        case Success(flow) => handleWebSocketMessages(flow)
        case Failure(err)  => complete(err.toString)
      }
    }

  def shutdown(): Unit = {
    Http().shutdownAllConnectionPools()
    // TODO Investigate this - is there a cleaner solution?
    //
    //  DO Nothing - we don't have a good handle to shutdown, but the actor system will be shutdown killing everything
  }

}
