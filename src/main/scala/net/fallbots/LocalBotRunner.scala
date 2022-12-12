package net.fallbots

import net.fallbots.bot.BotInterface
import net.fallbots.game.GameRoundResult.{GameOver, GameRound}
import net.fallbots.game.basicgame.BoardPrinter
import net.fallbots.game.state.{Board, BotAction}
import net.fallbots.game.{GameDef, GameRoundResult}
import net.fallbots.shared.BotId

import scala.util.Random

object LocalBotRunner {

  def notifyBotsOfGameStart(gameId: String, bots: Map[BotId, BotInterface]): Unit = {
    bots.values.foreach { bot =>
      bot.connected()
      bot.gameStarted(gameId)

    }
  }
  def applyMovesToBot(bots: Map[BotId, BotInterface], botStates: Map[BotId, Board]): Map[BotId, BotAction] = {
    bots
      .map { case (id, impl) =>
        impl.connected()
        impl.gameStarted("1")
        impl.getMove(botStates.getOrElse(id, throw new IllegalStateException(s"No info found for bot $id")))
      }
      .map(v => v.botId -> v)
      .toMap
  }

  def runGame(gameId: String, gameDef: GameDef, random: Random, bots: Map[BotId, BotInterface]): BotId = {
    val (game, initialStates) = gameDef.createGame(random, bots.keySet.toList)

    notifyBotsOfGameStart(gameId, bots)
    var nextActions = applyMovesToBot(bots, initialStates)

    var gameWinner: Option[BotId] = None
    while (gameWinner.isEmpty) {
      println("\n\nApplying round\n-----------------")
      val roundResult: GameRoundResult = game.applyRound(nextActions)
      BoardPrinter.printBoard(game.currentBoard)
      roundResult match {
        case GameOver(res) => gameWinner = Some(res)
        case GameRound(botStates) => {
          nextActions = applyMovesToBot(bots, botStates)
          println("To apply:")
          println(nextActions.values.foreach(println))
        }
      }
      Thread.sleep(2000)
    }

    gameWinner.get
  }
}
