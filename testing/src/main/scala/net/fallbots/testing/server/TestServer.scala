package net.fallbots.testing.server

import net.fallbots.bot.{BotRunner, SimpleBot}
import net.fallbots.server.FallBotsServer
import net.fallbots.shared.BotId

/**
 * TestServer is used for experimenting with the game server.  The
 */
object TestServer {
  def main(args: Array[String]): Unit = {

    val server = FallBotsServer.runServer(args).get
    println("Starting test bots")
    Thread.sleep(1000)

    val port = server.config.port
    val b1 = new Thread(new BotRunner("localhost", port, 1, "abc", new SimpleBot(BotId(1)))).start()
    val b2 = new Thread(new BotRunner("localhost", port, 2, "abc", new SimpleBot(BotId(2)))).start()
  }

}
