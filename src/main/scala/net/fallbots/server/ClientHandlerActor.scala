package net.fallbots.server

import net.fallbots.message.{FBMessage, RegistrationResponse, StatusMessage}

import scala.concurrent.duration._

class ClientHandlerActor extends WebsocketHandlerActor {

//  // test
//  var counter = 0
//  as.scheduler.scheduleAtFixedRate(0.seconds, 0.5.second)(() => {
//    counter = counter + 1
//    self ! counter
//  })

  def mainReceive: Receive = {
    // replies with "hello XXX"
    case m: FBMessage =>
      println("Got message: :" + m)
      down ! RegistrationResponse(true)

    // passes any int down the websocket
    case n: Int =>
      println(s"client actor received $n")
      down ! StatusMessage(n)
  }
}
