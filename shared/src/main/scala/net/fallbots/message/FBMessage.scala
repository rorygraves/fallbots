package net.fallbots.message

import net.fallbots.game.state.Board.BotState
import net.fallbots.game.state.{Board, BotAction, Direction, Position, SquareType, WallType}
import net.fallbots.message.GameMessage.{FBRequestGame, GameOver}
import net.fallbots.shared.BotId
import upickle.default.{macroRW, ReadWriter => RW}

object MessageImplicits {
  implicit val clientRegisterRW: RW[RegisterMessage] = macroRW

  implicit val serverRegistrationRespRW: RW[RegistrationResponse] = macroRW

  implicit val gameAwaitingRW: RW[GameMessage.AwaitingGame.type] = macroRW
  implicit val gameAssignedRW: RW[GameAssigned]                  = macroRW

  implicit val pingRW: RW[ServerPing.type] = macroRW
  implicit val pongRW: RW[ClientPong.type] = macroRW

  implicit val botIdRW: RW[BotId]                                  = macroRW
  implicit val directionNorthRW: RW[Direction.North.type]          = macroRW
  implicit val directionEastRW: RW[Direction.East.type]            = macroRW
  implicit val directionWestRW: RW[Direction.West.type]            = macroRW
  implicit val directionSouthRW: RW[Direction.South.type]          = macroRW
  implicit val botStateRW: RW[BotState]                            = macroRW
  implicit val moveActionBackwardsRW: RW[BotAction.Backwards.type] = macroRW
  implicit val moveActionForwardsRW: RW[BotAction.Forward.type]    = macroRW
  implicit val moveActionLeftRW: RW[BotAction.RotateLeft.type]     = macroRW
  implicit val moveActionRightRW: RW[BotAction.RotateRight.type]   = macroRW
  implicit val moveActionNoneRW: RW[BotAction.None.type]           = macroRW
  implicit val moveActionRW: RW[BotAction]                         = macroRW
  implicit val directionRW: RW[Direction.Direction]                = macroRW
  implicit val positionRW: RW[Position]                            = macroRW
  implicit val wallTypeNoneRW: RW[WallType.None.type]              = macroRW
  implicit val wallTypeSolidRW: RW[WallType.Solid.type]            = macroRW
  implicit val wallTypeRW: RW[WallType]                            = macroRW
  implicit val squareTypeEmptyRW: RW[SquareType.Empty.type]        = macroRW
  implicit val squareTypeRW: RW[SquareType]                        = macroRW
  implicit val squareStateRW: RW[Board.SquareState]                = macroRW
  implicit val boardRW: RW[Board]                                  = macroRW

  implicit val gameMoveRequestRW: RW[GameMessage.GameMoveRequest]   = macroRW
  implicit val gameMoveResponseRW: RW[GameMessage.GameMoveResponse] = macroRW

  implicit val gameOverRW: RW[GameOver]              = macroRW
  implicit val requestGameRW: RW[FBRequestGame.type] = macroRW
  implicit val messageRW: RW[FBMessage]              = macroRW

}

sealed trait FBMessage
trait FBClientMessage
trait FBServerMessage

case class RegisterMessage(id: Int, secret: String)                 extends FBMessage with FBClientMessage
case class RegistrationResponse(accepted: Boolean, message: String) extends FBMessage with FBServerMessage

case object ServerPing extends FBMessage with FBServerMessage
case object ClientPong extends FBMessage with FBClientMessage

// game messages from client to server
trait FBClientGameMessage extends FBClientMessage

// game messages from server to client
trait FBServerGameMessage extends FBServerMessage

case class GameAssigned(gameId: String) extends FBMessage with FBServerGameMessage

object GameMessage {

  case object AwaitingGame extends FBMessage with FBServerGameMessage

  case object FBRequestGame extends FBMessage with FBClientMessage

  case class GameOver(winner: Option[Int]) extends FBMessage with FBServerGameMessage

  case class GameMoveRequest(round: Int, board: Board)       extends FBMessage with FBServerGameMessage
  case class GameMoveResponse(round: Int, action: BotAction) extends FBMessage with FBClientGameMessage

}
