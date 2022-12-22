package net.fallbots.server

import akka.actor.ActorSystem
import net.fallbots.bot.{BotRunner, SimpleBot}
import net.fallbots.server.cmdline.{CmdLineParser, Config}
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

    val gameManager = as.actorOf(GameManager.props(1), "GameManager")
    val botManager  = as.actorOf(BotManager.props(gameManager), "botManager")

    logger.info("FallBots Server starting")
    val routing = new Routing(as, botManager)

    routing.startServer(config.port)

    println("Starting test bot")
    Thread.sleep(1000)

    new BotRunner("localhost", config.port, 1, "abc", new SimpleBot(BotId(1))).run()
  }
}
