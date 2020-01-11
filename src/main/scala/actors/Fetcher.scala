package actors

import akka.actor.{Actor, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import messages._
import org.jsoup.Jsoup


case class Fetcher(workersCount: Int) extends Actor {

  var router: Router = {
    val workers = Vector.fill(workersCount) {
      val r = context.actorOf(Props[FetcherWorker])
      context.watch(r)
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), workers)
  }

  override def receive: Receive = {
    case work => router.route(work, sender)
  }
}

class FetcherWorker extends Actor {

  override def receive: Receive = {
    case Fetch(url) =>

      try {
        sender ! Fetched(url, Jsoup.connect(url).get())
      } catch {
        case _: Throwable =>
      }
  }

}
