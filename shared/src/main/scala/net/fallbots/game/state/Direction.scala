package net.fallbots.game.state

object Direction {
  def opposite(direction: Direction): Direction = direction match {
    case North => South
    case East  => West
    case South => North
    case West  => East
  }

  def fromInt(i: Int): Direction = i match {
    case 0 => North
    case 1 => East
    case 2 => South
    case 3 => West
    case _ =>
      throw new IllegalStateException(s"0 <= $i <=3 must be true here")
  }

  sealed abstract class Direction(card: Int, xOff: Int, yOff: Int) {
    def nextPosition(position: Position): Position = Position(position.x + xOff, position.y + yOff)

    def rotateLeft: Direction  = fromInt((card + 3) % 4)
    def rotateRight: Direction = fromInt((card + 1) % 4)
  }

  // 0,0 is bottom left
  case object North extends Direction(0, 0, 1)
  case object East  extends Direction(1, 1, 0)
  case object South extends Direction(2, 0, -1)
  case object West  extends Direction(3, -1, 0)
}
