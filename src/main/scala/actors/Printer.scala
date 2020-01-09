package actors

import akka.actor.Actor
import messages._


class Printer extends Actor {

  override def receive: Receive = {
    case Print(str) =>
      println(str)
  }



}
