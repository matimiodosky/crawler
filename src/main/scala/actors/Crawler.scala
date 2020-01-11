package actors

import java.io.FileInputStream
import java.net.URI
import java.util.Properties

import akka.actor.{Actor, ActorRef, Props, _}
import akka.pattern.ask
import akka.util.Timeout
import messages._
import util.URLUtil
import util.newURLStrategy.{URLConsumer, URLConsumerProvider}

import scala.collection.immutable.List
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps


class Crawler extends Actor {

  val prop = new Properties()
  prop.load(new FileInputStream("application.properties"))

  val parser: ActorRef = context.actorOf(Props[Parser])
  var client: ActorRef = ActorRef.noSender
  var history: ActorRef = context.actorOf(Props(History(prop.getProperty("historyWorkers") toInt)))
  var historyStats: ActorRef = context.actorOf(Props[HistoryStats])
  val fetcher: ActorRef = context.actorOf(Props(Fetcher(prop.getProperty("fetcherWorkers") toInt)))
  var toValidate: List[String] = List()
  val newURLConsumer: URLConsumer = URLConsumerProvider.getConsumer(prop.getProperty("URLConsumer"))

  implicit val timeout: Timeout = Timeout(5 seconds)

  override def receive: Receive = {

    case Start(url) =>
      client = sender
      Await.result(history ? Clean(), 1 days)
      history ! ValidateAsNewURL(url)

    case Fetched(url, html) => parser ! Parse(url, html)

    case Parsed(urls) =>
      val max: Int = prop.getProperty("maxToValidate").toInt
      toValidate = toValidate ::: urls.filter(url => URLUtil.getHost(url).equals(prop.getProperty("pageFilter")))
      toValidate.take(max).foreach(history ! ValidateAsNewURL(_))
      toValidate = toValidate.drop(max)

    case NewURL(url) =>
      fetcher ! Fetch(url)
      newURLConsumer.onNewURL(url)

    case Stats() =>
      historyStats ! Stats()

    case StatsResponse(count, perSecond, perURL, time) =>
      println("Time: " + time + " Count: " + count + " -- Per Second: " + perSecond + "  -- Per URL (millis): " + perURL + " -- To Validate: " + toValidate.size)

  }

}
