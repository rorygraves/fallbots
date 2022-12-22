package net.fallbots.game.basicgame

import net.fallbots.game.state.Board.SquareState
import net.fallbots.game.state.{Board, Direction, Position, WallType}

object BoardPrinter {
  def printBoard(board: Board): Unit = {
    var first = true
    for (y <- (board.height - 1).to(0, -1)) {

      val row = (0 until board.width).map(x => board.squares(Position(x, y)))

      if (first)
        printTopRow(row)
      first = false
      printMainRow(row)
      printBottomRow(row)
    }

    println(s" target: ${board.target}")
    board.bots.keys.toList.sorted.foreach { botId =>
      val botPos   = board.bots(botId)
      val botState = board.squares(botPos).bot.get
      println(s" ${botId.id} -> ${botPos.x},${botPos.y} ${botState.direction}")
    }
    // TODO Tidy this up

    def northCharForWallType(wallType: WallType): Char = {
      wallType match {
        case WallType.Solid => '-'
        case WallType.None  => ' '
      }
    }

    def southCharForWallType(wallType: WallType): Char = {
      wallType match {
        case WallType.Solid => '-'
        case WallType.None  => ' '
      }
    }

    def westCharForWallType(wallType: WallType): Char = {
      wallType match {
        case WallType.Solid => '|'
        case WallType.None  => ' '
      }
    }

    def printTopRow(row: Seq[SquareState]): Unit = {
      print("+")
      row.foreach { sq =>
        print(sq.walls.get(Direction.North).map(northCharForWallType).getOrElse(' '))
        print("+")
      }
      println()
    }

    def printMainRow(row: Seq[SquareState]): Unit = {
      row.foreach { sq =>
        print(sq.walls.get(Direction.West).map(westCharForWallType).getOrElse(' '))
        print(sq.bot.map("" + _.botId.id).getOrElse("."))
      }
      println("|")
    }

    def printBottomRow(row: Seq[SquareState]): Unit = {
      print("+")
      row.foreach { sq =>
        print(sq.walls.get(Direction.South).map(southCharForWallType).getOrElse(' '))
        print("+")
      }
      println()
    }
  }
}
