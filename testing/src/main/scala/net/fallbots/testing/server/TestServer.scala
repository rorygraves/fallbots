package net.fallbots.testing.server

import net.fallbots.bot.{BotInterface, BotRunner, SimpleBot}
import net.fallbots.server.FallBotsServer
import net.fallbots.shared.BotId

/** TestServer is used for experimenting with the game server. The
  */
object TestServer {
  def main(args: Array[String]): Unit = {

    val server = FallBotsServer.runServer(args).get
    println("Starting test bots")
    Thread.sleep(1000)

    val port    = server.config.port
    val secrets = server.authService.secrets
    val b1      = startBot(1, port, id => new SimpleBot(id), secrets)
    val b2      = startBot(3, port, id => new SimpleBot(id), secrets)

    b1.join()
    b2.join()
  }

  def startBot(id: Int, port: Int, botCreatorFn: BotId => BotInterface, secrets: Map[BotId, String]): Thread = {
    val botId  = BotId(id)
    val secret = secrets.getOrElse(botId, throw new IllegalArgumentException(s"No secret for $botId"))
    val bot    = botCreatorFn(botId)
    val runner = new BotRunner("localhost", port, id, secret, bot)
    val t      = new Thread(runner)
    t.start()
    t
  }
}
