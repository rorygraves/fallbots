package net.fallbots.server

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import net.fallbots.message.{FBMessage, RegisterMessage, RegistrationResponse, StatusMessage}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ClientHandlerSpec
    extends AnyWordSpec
    with Matchers
    with Directives
    with ScalatestRouteTest {

  implicit val as: ActorSystem = ActorSystem("example-test")

  "A handler Websocket" should {

    "Send the expected messages" in {

      // tests:
      // create a testing probe representing the client-side
      val wsClient = WSProbe()

      val routing = new Routing(as)

      // WS creates a WebSocket request for testing
      WS("/connect", wsClient.flow) ~> routing.gameServerRouting ~>
        check {
          // check response for WS Upgrade headers
          isWebSocketUpgrade shouldEqual true

          def writeMsg(msg: FBMessage): Unit = {
            import upickle.default._
            wsClient.sendMessage(write(msg))
          }

          def expectMsg(msg: FBMessage): Unit = {
            import upickle.default._
            wsClient.expectMessage(write(msg))
          }

          expectMsg(StatusMessage(1))
          expectMsg(StatusMessage(2))
          expectMsg(StatusMessage(3))

          writeMsg(RegisterMessage(1, "abc"))
          expectMsg(RegistrationResponse(true))
        }
    }
  }
}
