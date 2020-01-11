import java.util.concurrent.TimeUnit

import actors._
import actors.history.HistoryProvider
import akka.actor.{ActorRef, ActorSystem, Props}
import messages.{Start, Stats}
import util.Configuration

import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import actors.Crawler

object Main  extends App {

  val system = ActorSystem("actorSystem")
  val printer = system.actorOf(Props[Printer])
  val history: ActorRef = system.actorOf(Props(HistoryProvider.getHistory(Configuration.getConfig("history"))))

  val crawler: ActorRef = system.actorOf(Props(Crawler(history)))

  crawler.tell(Start("https://www.infobae.com"), printer)
  system.scheduler.scheduleAtFixedRate(Duration(5 , TimeUnit.SECONDS) , Duration(5 , TimeUnit.SECONDS) , history, Stats())

}
