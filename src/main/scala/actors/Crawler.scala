package actors

import actors.history.HistoryProvider
import akka.actor.{Actor, ActorRef, Props, _}
import akka.pattern.ask
import akka.util.Timeout
import messages._
import util.{Configuration, URLConsumer, URLConsumerProvider, URLUtil}
import scala.collection.immutable.List
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class Crawler extends Actor {


  val parser: ActorRef = context.actorOf(Props[Parser])
  var client: ActorRef = ActorRef.noSender
  var history: ActorRef = context.actorOf(Props(HistoryProvider.getHistory(Configuration.getConfig("history"))))
  val fetcher: ActorRef = context.actorOf(Props(Fetcher(Configuration.getConfig("fetcherWorkers") toInt)))
  var toValidate: List[String] = List()
  val newURLConsumer: URLConsumer = URLConsumerProvider.getConsumer(Configuration.getConfig("URLConsumer"))

  implicit val timeout: Timeout = Timeout(5 seconds)

  override def receive: Receive = {

    case Start(url) =>
      client = sender
      Await.result(history ? Clean(), 1 days)
      history ! ValidateAsNewURL(url)

    case Fetched(url, html) => parser ! Parse(url, html)

    case Parsed(urls) =>
      val max: Int = Configuration.getConfig("maxToValidate").toInt
      toValidate = toValidate ::: urls.filter(url => URLUtil.getHost(url).equals(Configuration.getConfig("pageFilter")))
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
