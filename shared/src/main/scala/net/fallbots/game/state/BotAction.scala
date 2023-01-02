package net.fallbots.game.state

object BotAction {

  case object None        extends BotAction
  case object Forward     extends BotAction
  case object Backwards   extends BotAction
  case object RotateLeft  extends BotAction
  case object RotateRight extends BotAction
}

sealed trait BotAction
