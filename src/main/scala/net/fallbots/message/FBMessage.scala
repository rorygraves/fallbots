package net.fallbots.message

import upickle.default.{ReadWriter => RW, macroRW}

object MessageImplicits {
  implicit val clientRegisterRW: RW[RegisterMessage] = macroRW

  implicit val serverRegistrationRespRW: RW[RegistrationResponse] = macroRW

  implicit val gameRequestRW: RW[FBCGGameRequest.type] = macroRW

  implicit val gameAwaitingRW: RW[AwaitingGame.type] = macroRW
  implicit val gameAssignedRW: RW[GameAssigned]      = macroRW

  implicit val messageRW: RW[FBMessage] = macroRW

}
sealed trait FBMessage
trait FBClientMessage
trait FBServerMessage

case class RegisterMessage(id: Int, secret: String)                 extends FBMessage with FBClientMessage
case class RegistrationResponse(accepted: Boolean, message: String) extends FBMessage with FBServerMessage

// game messages from client to server
trait FBClientGameMessage   extends FBClientMessage
case object FBCGGameRequest extends FBMessage with FBClientGameMessage

// game messages from server to client
trait FBServerGameMessage extends FBServerMessage

case object AwaitingGame                extends FBMessage with FBServerGameMessage
case class GameAssigned(gameId: String) extends FBMessage with FBServerGameMessage
