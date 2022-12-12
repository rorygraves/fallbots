package net.fallbots.game.state

import net.fallbots.game.state.Board.{BotState, SquareState}
import net.fallbots.shared.BotId

object Board {

  case class SquareState(
      pos: Position,
      sqType: SquareType,
      walls: Map[Direction.Direction, WallType],
      bot: Option[BotState]
  ) {
    def canMove(direction: Direction.Direction): Boolean = walls.get(direction).forall(_.canTraverse())

    def isEmpty: Boolean = sqType == SquareType.Empty && bot.isEmpty

  }

  case class BotState(botId: BotId, direction: Direction.Direction)

  def createEmpty(width: Int, height: Int, target: Position): Board = {
    val baseMap =
      (for (
        x <- 0 until width;
        y <- 0 until height
      ) yield {
        var walls = Map.empty[Direction.Direction, WallType]
        if (x == 0) walls = walls + (Direction.West -> WallType.Solid)
        if (y == 0) walls = walls + (Direction.South -> WallType.Solid)
        if (x == width - 1) walls = walls + (Direction.East -> WallType.Solid)
        if (y == height - 1) walls = walls + (Direction.North -> WallType.Solid)
        SquareState(Position(x, y), SquareType.Empty, walls, None)
      }).map(sq => sq.pos -> sq).toMap

    Board(width, height, baseMap, target, Map.empty)
  }
}

case class Board(
    width: Int,
    height: Int,
    squares: Map[Position, SquareState],
    target: Position,
    bots: Map[BotId, Position]
) {
  def getWinner: Option[BotId] = bots.find(v => v._2 == target).map(_._1)

  def applyBotAction(botId: BotId, action: BotAction): Board = {

    val position = bots.getOrElse(botId, throw new IllegalStateException(s"Move applied for missing bot $botId"))
    val currentSq =
      squares.getOrElse(position, throw new IllegalStateException(s"Bot $botId at missing position $position"))
    val botState =
      currentSq.bot.getOrElse(throw new IllegalStateException(s"Bot not within stated square $botId, $position"))

    def withNewDirection(newDir: Direction.Direction): Board = {

      val newSq = currentSq.copy(bot = Some(botState.copy(direction = newDir)))
      this.copy(squares = this.squares + (position -> newSq))
    }

    def moveBot(direction: Direction.Direction): Board = {

      if (!currentSq.canMove(direction))
        this
      else {
        val newPos = direction.nextPosition(position)

        squares.get(newPos) match {
          case Some(sq) if sq.isEmpty =>
            val targetSq   = sq.copy(bot = Some(botState))
            val srcSq      = currentSq.copy(bot = None)
            val newSquares = this.squares ++ List(newPos -> targetSq, position -> srcSq)
            val newBots    = bots + (botId -> newPos)
            this.copy(squares = newSquares, bots = newBots)
          case _ => // we cannot move there
            this
        }
      }
    }

    action.moveAction match {
      case MoveAction.None =>
        this
      case MoveAction.RotateLeft =>
        withNewDirection(botState.direction.rotateLeft)
      case MoveAction.RotateRight =>
        withNewDirection(botState.direction.rotateLeft)
      case MoveAction.Forward =>
        moveBot(botState.direction)
      case MoveAction.Backwards =>
        moveBot(Direction.opposite(botState.direction))

    }
  }

  def tryAndPlacePlayer(botId: BotId, pos: Position, dir: Direction.Direction): Either[Unit, Board] = {
    squares.get(pos) match {
      case Some(square) if square.bot.isDefined =>
        Left(())
      case Some(square) =>
        Right(
          this.copy(
            squares = squares + (pos -> square.copy(bot = Some(BotState(botId, dir)))),
            bots = bots + (botId     -> pos)
          )
        )
      case None =>
        throw new IllegalStateException(s"Board position $pos does not exist")
    }
  }

}
