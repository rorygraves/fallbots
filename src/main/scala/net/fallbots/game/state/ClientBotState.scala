package net.fallbots.game.state

import net.fallbots.game.state.Board.SquareState
import net.fallbots.shared.BotId

case class ClientBotState(
    botId: BotId,
    position: Position,
    direction: Direction.Direction,
    goal: Position,
    boardState: Seq[SquareState]
) {}
