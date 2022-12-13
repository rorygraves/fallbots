package net.fallbots.game.state

trait WallType {
  def canTraverse: Boolean

}

object WallType {
  case object None extends WallType {
    override def canTraverse: Boolean = true
  }
  case object Solid extends WallType {
    override def canTraverse: Boolean = false
  }
}
