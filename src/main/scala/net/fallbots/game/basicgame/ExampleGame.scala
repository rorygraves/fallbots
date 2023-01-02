package net.fallbots.game.basicgame

import net.fallbots.game.state._
import net.fallbots.game.{Game, GameDef, GameRoundResult}
import net.fallbots.shared.BotId

import scala.annotation.tailrec
import scala.util.Random

object ExampleGame extends GameDef {

  @tailrec def placePlayers(
      board: Board,
      players: List[BotId],
      random: Random
  ): Board = {
    if (players.isEmpty)
      board
    else {
      val botId = players.head
      val rest  = players.tail
      val pos   = Position(random.nextInt(board.width), random.nextInt(board.height / 2))
      val dir   = Direction.fromInt(random.nextInt(4))

      board.tryAndPlacePlayer(botId, pos, dir) match {
        case Left(_) => // failed
          placePlayers(board, players, random) // try again
        case Right(newBoard) => // succeed
          placePlayers(newBoard, rest, random)
      }
    }
  }

  def filterBoardForBot(botId: BotId, board: Board): Board = {
    board // right now all players see entire game state
  }

  override def createGame(random: Random, playerList: List[BotId], maxRounds: Int): (Game, Map[BotId, Board]) = {

    val width        = 10
    val height       = 10
    val target       = Position(width / 2, height - 1)
    val initialBoard = Board.createEmpty(10, 10, target)

    val board = placePlayers(initialBoard, playerList, random)
    (new ExampleGame(board, maxRounds, random), generatePlayerInfos(board))
  }

  private def generatePlayerInfos(board: Board): Map[BotId, Board] =
    board.bots.keys.map(b => b -> filterBoardForBot(b, board)).toMap
}

class ExampleGame(initialBoard: Board, maxRounds: Int, random: Random) extends Game {

  private var _currentBoard: Board = initialBoard
  def currentBoard: Board          = _currentBoard

  private var _currentRound = 1
  def currentRound          = _currentRound

  @tailrec
  private final def applyMoves(moves: List[(BotId, BotAction)], board: Board): Board = {
    moves match {
      case (botId, action) :: xs =>
        applyMoves(xs, board.applyBotAction(botId, action))
      case Nil =>
        board
    }
  }

  override def applyRound(random: Random, moves: Map[BotId, BotAction]): GameRoundResult = {
    if (currentRound > maxRounds) throw new IllegalStateException("Game is over")
    val randomisedMoves = random.shuffle(moves.toList)
    _currentBoard = applyMoves(randomisedMoves, currentBoard)

    _currentRound = currentRound + 1

    currentBoard.getWinner match {
      case Some(winner) =>
        GameRoundResult.GameOver(Some(winner))
      case None =>
        if (_currentRound > maxRounds)
          GameRoundResult.GameOver(None)
        else
          GameRoundResult.GameRound(ExampleGame.generatePlayerInfos(currentBoard))
    }
  }
}
