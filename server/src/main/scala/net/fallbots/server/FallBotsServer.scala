package net.fallbots.server

import akka.actor.ActorSystem
import net.fallbots.server.FallBotsServer.logger
import net.fallbots.server.akkahttp.AkkaHttpServer
import net.fallbots.server.auth.AuthService
import net.fallbots.server.cmdline.{CmdLineParser, ServerImpl}
import net.fallbots.server.config.Config
import net.fallbots.server.jetty.JettyServer
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Random

object FallBotsServer {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    runServer(args)
  }

  /** Start the server given the command
    */
  def runServer(args: Array[String]): Option[FallBotsServer] = {
    CmdLineParser.parseCommandLine(args) match {
      case Some(config) =>
        try {
          val authService = AuthService.createService(config.gameServerConfig.noPlayers)
          authService.printSecrets()
          val server = new FallBotsServer(config, authService)
          server.start()
          Some(server)
        } catch {
          case t: Throwable =>
            t.printStackTrace()
            None
        }
      case _ =>
        // arguments are bad, error message will have been displayed
        None
    }
  }
}

class FallBotsServer(val config: Config.Config, val authService: AuthService) {
  private implicit var actorSystem: ActorSystem = null
  private var server: Option[AbstractServer]    = None
  private var _isRunning                        = false
  def isRunning                                 = _isRunning
  def start(): Unit = {
    actorSystem = ActorSystem("main")

    val gameManager = actorSystem.actorOf(GameManager.props(config.gameServerConfig, config.gameConfig), "GameManager")
    val botManager  = actorSystem.actorOf(BotManager.props(gameManager, authService), "BotManager")

    logger.info("FallBots Server starting")
    server = Some(config.serverImpl match {
      case ServerImpl.AkkaHttp =>
        val server = new AkkaHttpServer(actorSystem, botManager, gameManager)
        server.startServer(config.port)
        server
      case ServerImpl.Jetty =>
        val server = new JettyServer(actorSystem, botManager, gameManager, config.port)
        server.start()
        server
    })
    _isRunning = true
  }

  def stop(): Unit = {
    server.foreach(_.shutdown())
    server = None
    _isRunning = false
  }
}
