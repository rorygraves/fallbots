package net.fallbots.server

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import net.fallbots.message.{FBMessage, RegisterMessage, RegistrationResponse}
import net.fallbots.server.akkahttp.AkkaHttpServer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ClientHandlerSpec extends AnyWordSpec with Matchers with Directives with ScalatestRouteTest {

  implicit val as: ActorSystem = ActorSystem("example-test")

  "A handler Websocket" should {

    "Send the expected messages" in {

      // tests:
      // create a testing probe representing the client-side
      val wsClient = WSProbe()

      val gameManager = as.actorOf(GameManager.props(1))
      val botManager  = as.actorOf(BotManager.props(gameManager))

      import net.fallbots.message.MessageImplicits._
      val routing = new AkkaHttpServer(as, botManager, gameManager)

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

          writeMsg(RegisterMessage(1, "abc"))
          expectMsg(RegistrationResponse(accepted = true, ""))
        }
    }
  }
}
