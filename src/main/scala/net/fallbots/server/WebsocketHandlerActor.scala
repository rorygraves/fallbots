package net.fallbots.server

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, Sink, Source}
import akka.stream.{FlowShape, OverflowStrategy}
import net.fallbots.message.FBMessage
import net.fallbots.server.Routing.GetWebsocketFlow

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

abstract class WebsocketHandlerActor extends Actor {

  implicit val as: ActorSystem      = context.system
  implicit val ec: ExecutionContext = context.system.dispatcher

  val (down, publisher) = Source
    .actorRef[FBMessage](1000, OverflowStrategy.fail)
    .toMat(Sink.asPublisher(fanout = false))(Keep.both)
    .run()

  override def receive: Receive = initialReceive

  import upickle.default._

  def initialReceive: Receive = { case GetWebsocketFlow =>
    val flow = Flow.fromGraph(GraphDSL.create() { implicit b =>
      val textMsgFlow = b.add(
        Flow[Message]
          .mapAsync(1) {
            case tm: TextMessage =>
              tm.toStrict(3.seconds).map(m => read[net.fallbots.message.FBMessage](m.text))
            case bm: BinaryMessage =>
              // consume the stream
              bm.dataStream.runWith(Sink.ignore)
              Future.failed(new Exception("binary message rejected"))
          }
      )

      val pubSrc = b.add(Source.fromPublisher(publisher).map(m => TextMessage(write(m))))

      textMsgFlow ~> Sink.foreach[FBMessage](self ! _)
      FlowShape(textMsgFlow.in, pubSrc.out)
    })

    sender ! flow
    context.become(mainReceive)
  }

  def mainReceive: Receive
}
