package net.fallbots.message

import upickle.default.{ReadWriter => RW, macroRW}

sealed trait FBMessage
object FBMessage {
  implicit val rw: RW[FBMessage] = macroRW
}

sealed trait FBClientMessage extends FBMessage
object FBClientMessage {
  implicit val rw: RW[FBClientMessage] = macroRW
}

sealed trait FBServerMessage extends FBMessage
object FBServerMessage {
  implicit val rw: RW[FBServerMessage] = macroRW
}

object RegisterMessage {
  implicit val rw: RW[RegisterMessage] = macroRW
}

case class RegisterMessage(id: Int, secret: String) extends FBClientMessage

object RegistrationResponse {
  implicit val rw: RW[RegistrationResponse] = macroRW
}

case class RegistrationResponse(accepted: Boolean) extends FBServerMessage

object StatusMessage {
  implicit val rw: RW[StatusMessage] = macroRW
}

case class StatusMessage(count: Int) extends FBServerMessage
