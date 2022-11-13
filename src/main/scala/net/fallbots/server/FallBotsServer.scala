package net.fallbots.server

import akka.actor.ActorSystem
import net.fallbots.bot.{BotRunner, NoOpBot}
import net.fallbots.server.cmdline.{CmdLineParser, Config}
import org.slf4j.LoggerFactory

object FallBotsServer {

  val logger = LoggerFactory.getLogger(this.getClass)
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

    logger.info("FallBots Server starting")
    val routing = new Routing(as)

    routing.startServer(config.port)

    println("HERE")
    Thread.sleep(1000)

    new BotRunner("localhost", config.port, 1, "abc", new NoOpBot()).run()
  }
}
