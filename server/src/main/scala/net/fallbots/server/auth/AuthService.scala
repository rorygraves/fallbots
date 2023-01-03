package net.fallbots.server.auth

import net.fallbots.server.FallBotsServer
import net.fallbots.shared.BotId

import scala.io.Source
import scala.util.Random

object AuthService {
  private def getWords(numWords: Int, words: IndexedSeq[String], random: Random): List[String] = {
    (0 until numWords).map { _ =>
      words(random.nextInt(words.length))
    }.toList
  }

  def createService(numBots: Int, wordsPerSecret: Int = 3, random: Random = new Random()): AuthService = {
    val wordsList = Words.words

    val botsSecrets = (1 to numBots).map { id =>
      val words = getWords(wordsPerSecret, wordsList, random)
      BotId(id) -> words.mkString("-")
    }.toMap

    new AuthService(botsSecrets)
  }
}

class AuthService(val secrets: Map[BotId, String]) {
  def printSecrets(): Unit = {
    println()
    println(" Bot Secrets")
    println("---------------------")
    secrets.toList.sortBy(_._1).foreach { case (botId, secret) =>
      println(s"  ${botId.id}    ->  $secret")
    }
    println
  }

  def authorise(botId: BotId, presentedSecret: String): Boolean = {
    secrets.get(botId) match {
      case Some(secret) if secret == presentedSecret => true
      case _                                         => false
    }
  }
}
