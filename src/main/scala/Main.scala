import actors._
import akka.actor.{ActorRef, ActorSystem, Props}
import messages.Start


object Main  extends App {


  val system = ActorSystem("actorSystem")
  val printer = system.actorOf(Props[Printer])
  val crawler: ActorRef = system.actorOf(Props[Crawler])

  crawler.tell(Start("https://www.infobae.com/"), printer)
}
