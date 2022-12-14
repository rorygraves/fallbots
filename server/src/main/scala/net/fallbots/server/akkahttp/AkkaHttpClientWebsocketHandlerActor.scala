package net.fallbots.server.akkahttp

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, Sink, Source}
import akka.stream.{FlowShape, OverflowStrategy}
import net.fallbots.message.FBMessage
import net.fallbots.server.ClientHandlerActor
import net.fallbots.server.akkahttp.AkkaHttpServer.GetWebsocketFlow

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object AkkaHttpClientWebsocketHandlerActor {
  def props(botManager: ActorRef, gameManager: ActorRef): Props = Props(
    new AkkaHttpClientWebsocketHandlerActor(botManager, gameManager)
  )
}

/** Wrap all the logic of setting up message flows for the websocket connection. Subclasses simply need to implement
  * 'mainReceive to do their logic. To send a message to the socket use down ! FPMessageInstance.
  */
class AkkaHttpClientWebsocketHandlerActor(botManager: ActorRef, gameManager: ActorRef)
    extends ClientHandlerActor(botManager, gameManager) {

  implicit val as: ActorSystem      = context.system
  implicit val ec: ExecutionContext = context.system.dispatcher

  val (down, publisher) = Source
    .actorRef[FBMessage](1000, OverflowStrategy.fail)
    .toMat(Sink.asPublisher(fanout = false))(Keep.both)
    .run()

  override def send(msg: FBMessage): Unit = {
    down ! msg
  }
  override def receive: Receive = initialReceive

  import upickle.default._

  private def initialReceive: Receive = { case GetWebsocketFlow =>
    val flow = Flow.fromGraph(GraphDSL.create() { implicit b =>
      val textMsgFlow = b.add(
        Flow[Message]
          .mapAsync(1) {
            case tm: TextMessage =>
              import net.fallbots.message.MessageImplicits._
              tm.toStrict(3.seconds).map(m => read[net.fallbots.message.FBMessage](m.text))
            case bm: BinaryMessage =>
              // consume the stream
              bm.dataStream.runWith(Sink.ignore)
              Future.failed(new Exception("binary message rejected"))
          }
      )

      import net.fallbots.message.MessageImplicits._
      val pubSrc = b.add(Source.fromPublisher(publisher).map(m => TextMessage(write(m))))

      textMsgFlow ~> Sink.foreach[FBMessage](self ! _)
      FlowShape(textMsgFlow.in, pubSrc.out)
    })

    sender() ! flow
    context.become(mainReceive)
  }

  override def shutdown(): Unit = {
    println("Client handler shutting down, trigger close of websocket")
    down ! PoisonPill
  }

}
