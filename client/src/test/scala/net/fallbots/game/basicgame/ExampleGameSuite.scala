package net.fallbots.game.basicgame

import net.fallbots.bot.{LocalBotRunner, SimpleBot}
import net.fallbots.shared.BotId
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Random

class ExampleGameSuite extends AnyFunSuite {
  test("Basic game init") {

    val random = new Random(5)
    val bots =
      Map(BotId(1) -> new SimpleBot(BotId(1)), BotId(2) -> new SimpleBot(BotId(2)), BotId(3) -> new SimpleBot(BotId(3)))

    val winner = LocalBotRunner.runGame("game1", ExampleGame, random, bots, 100)
    println("Winner = " + winner)
  }
}
