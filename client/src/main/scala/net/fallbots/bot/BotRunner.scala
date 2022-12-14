package net.fallbots.bot

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import net.fallbots.bot.BotRunner.ClosedException
import net.fallbots.message._
import org.slf4j.{Logger, LoggerFactory}

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue, TimeUnit}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object BotRunner {
  class ClosedException extends Throwable
}

/** BotRunner handles the communication with the game server. Bots are created by implementing and supplying the
  * BotInterface.
  * @param hostname
  *   Hostname of the game server
  * @param port
  *   Port of the game server
  * @param botId
  *   The bot id to register as
  * @param botSecret
  *   The bot secret for authentication
  * @param bot
  *   The bot implementation.
  */
class BotRunner(hostname: String, port: Int, botId: Int, botSecret: String, bot: BotInterface) extends Runnable {

  val logger: Logger           = LoggerFactory.getLogger(s"BotRunner-$botId")
  implicit val as: ActorSystem = ActorSystem.apply("bot-runner")

  import upickle.default._

  private val incomingQueue: BlockingQueue[FBMessage] = new LinkedBlockingQueue[FBMessage]()

  // create a output going
  private val sourceDecl                  = Source.queue[TextMessage](bufferSize = 2, OverflowStrategy.backpressure)
  private val (outgoingQueue, sourceFlow) = sourceDecl.preMaterialize()

  private def connect(): Unit = {
    logger.info(s"Connecting to $hostname:$port as bot $botId")

    // each message received is passed to the incomingQueue for consumption by the bot.
    val msgSink: Sink[Message, Future[Done]] =
      Sink.foreach {
        case message: TextMessage.Strict =>
          import net.fallbots.message.MessageImplicits._
          val decoded = read[FBMessage](message.text)
          if (decoded == ServerPing)
            sendMessage(ClientPong)
          else
            incomingQueue.put(decoded)
        case _ =>
          println("HERE 1234")
      }

    // the Future[Done] is the materialized value of Sink.foreach
    // and it is completed when the stream completes
    val flow: Flow[Message, Message, Future[Done]] =
      Flow.fromSinkAndSourceMat(msgSink, sourceFlow)(Keep.left)

    // upgradeResponse is a Future[WebSocketUpgradeResponse] that
    // completes or fails when the connection succeeds or fails
    // and closed is a Future[Done] representing the stream completion from above
    val (upgradeResponse, closed) =
      Http().singleWebSocketRequest(
        WebSocketRequest(s"ws://$hostname:$port/connect"),
        flow
      )

    val connected = upgradeResponse.map { upgrade =>
      // just like a regular http request we can access response status which is available via upgrade.response.status
      // status code 101 (Switching Protocols) indicates that server support WebSockets
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        logger.info("Successfully upgraded connection to websocket")
        Done
      } else {
        throw new RuntimeException(
          s"Connection failed: ${upgrade.response.status}"
        )
      }
    }

    // in a real application you would not side effect here
    // and handle errors more carefully

    Await.result(connected, Duration.apply(1, TimeUnit.MINUTES))

    closed.foreach(_ => logger.info("CLOSED not handled"))
  }

  /** send the bot authentication message - and receive and validate the reply.
    */
  def authenticate(): Unit = {
    logger.info("Sending authentication")
    sendMessage(RegisterMessage(botId, botSecret))
    val msg = receiveMessage()
    msg match {
      case RegistrationResponse(accepted, msg) =>
        if (accepted)
          logger.info("Registration accepted")
        else {
          logger.warn(s"Registration rejected with message: $msg - shutting down")
          throw new ClosedException
        }
        logger.info(s"Got registration message - accepted: $accepted $msg")
      case _ =>
        throw new IllegalStateException("Got unexpected message during registration: " + msg)
    }
  }

  def botLoop(): Unit = {
    logger.info("Starting main bot loop")
    sendMessage(GameMessage.FBRequestGame)
    while (true) {
      val msg = receiveMessage()
      msg match {
        case GameMessage.AwaitingGame =>
          logger.info("Awaiting game")
        case GameAssigned(gameId) =>
          gameLoop(gameId)

          // game completed request a new game
          sendMessage(GameMessage.FBRequestGame)
        case _ => logger.info("Unhandled message: " + msg)
      }
    }
  }

  private def gameLoop(gameId: String): Unit = {
    logger.info(s"G$gameId assigned")
    var gameOver = false
    bot.gameStarted(gameId)
    while (!gameOver) {
      val msg = receiveMessage()
      msg match {
        case GameMessage.GameMoveRequest(round, board) =>
          logger.info("move request")
          val action = bot.getMove(board)
          sendMessage(GameMessage.GameMoveResponse(round, action))
        case GameMessage.GameOver(winner) =>
          bot.gameEnded()
          val winnerStr = winner.map(i => s"Bot $i").getOrElse("None")
          logger.info(s"Game over winner: $winnerStr")
          gameOver = true
      }
    }

  }

  /** Send a message to the server
    *
    * @param message
    *   The target message to send
    */
  private def sendMessage(message: FBMessage): Unit = {
    logger.debug(s"Sending message: $message")
    import net.fallbots.message.MessageImplicits._
    val outGoingMsg = write(message, indent = 4)
    outgoingQueue.offer(TextMessage(outGoingMsg))
  }

  /** Wait for a message from the server
    */
  private def receiveMessage(): FBMessage = {
    val m = incomingQueue.take()
    logger.debug("Got message from server: " + m)
    m
  }

  override def run(): Unit = {
    logger.info("Starting")
    connect()
    authenticate()
    // notify the bot that we are successfully connected
    bot.connected()

    botLoop()
  }
}
