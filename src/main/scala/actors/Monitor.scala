package actors

import akka.actor.{Actor, ActorRef}
import messages.{Print, StatsRequest, StatsResponse, Tick}

case class Monitor(crawler: ActorRef, printer: ActorRef) extends Actor {

  var lastCount = 0
  var lastTime = 0l
  private val firstTime = System.currentTimeMillis()

  override def receive: Receive = {

    case Tick() =>
      crawler ! StatsRequest(lastCount)

    case StatsResponse(currentCount, newsCount) =>
      val time = System.currentTimeMillis()
      val timeElapsed = time - lastTime
      lastCount = currentCount
      lastTime = time
      printer ! Print("Webs fetched per second lately: " + 1000 * newsCount / timeElapsed)
      printer ! Print("Webs fetched per second: " + 1000 * currentCount / (time - firstTime))
      printer ! Print("Webs fetched : " + currentCount)

  }

}
