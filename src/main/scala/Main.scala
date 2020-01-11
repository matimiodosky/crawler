import java.util.concurrent.TimeUnit


import actors._
import akka.actor.{ActorRef, ActorSystem, Props}
import messages.{Start, Stats}

import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global



object Main  extends App {

  val system = ActorSystem("actorSystem")
  val printer = system.actorOf(Props[Printer])
  val crawler: ActorRef = system.actorOf(Props[Crawler])

  crawler.tell(Start("https://www.infobae.com"), printer)
  system.scheduler.scheduleAtFixedRate(Duration.Zero, Duration(5 , TimeUnit.SECONDS) , crawler, Stats())

}
