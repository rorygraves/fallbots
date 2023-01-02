package net.fallbots.bot

import net.fallbots.shared.BotId

object SampleBotApp {

  def main(args: Array[String]): Unit = {
    if (args.length != 4) {
      println("Usage:  SampleBotApp <serverHost> <serverPort> <botId> <botSecret>")
      System.exit(1)
    }

    val host      = args(0)
    val port      = args(1).toInt
    val botId     = args(2).toInt
    val botSecret = args(3)

    new BotRunner(host, port, botId, botSecret, new SimpleBot(BotId(1))).run()

  }
}
