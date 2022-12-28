package net.fallbots.server

import akka.actor.ActorSystem
import net.fallbots.bot.{BotRunner, NoOpBot, SimpleBot}
import net.fallbots.server.akkahttp.AkkaHttpServer
import net.fallbots.server.cmdline.{CmdLineParser, Config, ServerImpl}
import net.fallbots.server.jetty.JettyServer
import net.fallbots.shared.BotId
import org.slf4j.{Logger, LoggerFactory}

object FallBotsServer {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  def main(args: Array[String]): Unit = {

    CmdLineParser.parseCommandLine(args) match {
      case Some(config) =>
        try {
          runServer(config)
        } catch {
          case t: Throwable =>
            t.printStackTrace()
        }
      case _ =>
      // arguments are bad, error message will have been displayed
    }

  }
  def runServer(config: Config): Unit = {
    implicit val as: ActorSystem = ActorSystem("main")

    val gameManager = as.actorOf(GameManager.props(2), "GameManager")
    val botManager  = as.actorOf(BotManager.props(gameManager), "BotManager")

    logger.info("FallBots Server starting")
    config.serverImpl match {
      case ServerImpl.AkkaHttp =>
        val server = new AkkaHttpServer(as, botManager, gameManager)
        server.startServer(config.port)
      case ServerImpl.Jetty =>
        val server = new JettyServer(as, botManager, gameManager, config.port)
        server.start()
    }

    println("Starting test bots")
    Thread.sleep(1000)

    new Thread(new BotRunner("localhost", config.port, 1, "abc", new SimpleBot(BotId(1)))).start()
    new Thread(new BotRunner("localhost", config.port, 2, "abc", new SimpleBot(BotId(2)))).start()
  }
}
