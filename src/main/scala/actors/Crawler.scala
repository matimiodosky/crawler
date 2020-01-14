package actors

import akka.actor.{Actor, ActorRef, Props, _}
import akka.pattern.ask
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akka.util.Timeout
import messages._
import util.{Configuration, URLConsumer, URLConsumerProvider, URLUtil}

import scala.collection.immutable.List
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

case class Crawler(history: ActorRef) extends Actor {

  var router: Router = {
    val workers = Vector.fill(Configuration.getConfig("crawlerWorkers") toInt) {
      val r = context.actorOf(Props(CrawlerWorker(history)))
      context.watch(r)
      ActorRefRoutee(r)
    }

    Router(RoundRobinRoutingLogic(), workers)
  }

  override def receive: Receive = {
    case work => router.route(work, sender)
  }
}

case class CrawlerWorker(history: ActorRef) extends Actor {

  val parser: ActorRef = context.actorOf(Props[Parser])
  var client: ActorRef = ActorRef.noSender
  val fetcher: ActorRef = context.actorOf(Props(Fetcher(Configuration.getConfig("fetcherWorkers") toInt)))
  val graph: ActorRef = context.actorOf(Props[Graph])
  var toValidate: List[String] = List()
  val newURLConsumer: URLConsumer = URLConsumerProvider.getConsumer(Configuration.getConfig("URLConsumer"))
  val start: Long = System.currentTimeMillis()
  implicit val timeout: Timeout = Timeout(5 seconds)


  override def receive: Receive = {

    case Start(url) =>
      client = sender
      Await.result(history ? Clean(), 1 days)
      fetcher ! Fetch(url)

    case Fetched(url, html) => parser ! Parse(url, html)

    case Parsed(origin, urls) =>
      var max = Configuration.getConfig("maxToValidate").toInt

      urls.foreach(url => graph ! NewVertex(origin, url))

      toValidate = toValidate ::: urls
        .filter(url => URLUtil.getHost(url).equals(Configuration.getConfig("pageFilter")))
      toValidate.take(max).foreach(history ! ValidateAsNewURL(_))
      toValidate = toValidate.drop(max)

    case NewURL(url) =>
      fetcher ! Fetch(url)
      newURLConsumer.onNewURL(url)

    case Stats() =>
      history ! Stats()

    case StatsResponse(count, perSecond, perURL, time) =>
      println("Time: " + time + " Count: " + count + " -- Per Second: " + perSecond + "  -- Per URL (millis): " + perURL + " -- To Validate: " + toValidate.size)

  }
}
