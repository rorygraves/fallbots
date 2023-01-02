package net.fallbots.shared

case class BotId(id: Int) extends Comparable[BotId] {
  override def compareTo(o: BotId): Int = id.compareTo(o.id)
}
