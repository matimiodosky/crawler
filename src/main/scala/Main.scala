import java.util.concurrent.TimeUnit

import actors._
import akka.actor.{ActorRef, ActorSystem, Props}
import messages.{Start, Tick}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


object Main  extends App {


  val system = ActorSystem("actorSystem")
  val printer = system.actorOf(Props[Printer])
  val crawler: ActorRef = system.actorOf(Props[Crawler])
  val monitor: ActorRef = system.actorOf(Props(Monitor(crawler, printer)))
  val dumper: ActorRef = system.actorOf(Props(Dumper(crawler)))

  crawler.tell(Start("https://www.youtube.com/"), printer)
  system.scheduler.scheduleAtFixedRate(Duration.Zero, Duration(5, TimeUnit.SECONDS) , monitor, Tick())
  system.scheduler.scheduleAtFixedRate(Duration.Zero, Duration(30, TimeUnit.SECONDS) , dumper, Tick())

}
