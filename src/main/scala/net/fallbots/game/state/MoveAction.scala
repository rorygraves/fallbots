package net.fallbots.game.state

object MoveAction {

  case object None        extends MoveAction
  case object Forward     extends MoveAction
  case object Backwards   extends MoveAction
  case object RotateLeft  extends MoveAction
  case object RotateRight extends MoveAction
}

sealed trait MoveAction
