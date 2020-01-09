package actors

import akka.actor.Actor
import akka.pattern.pipe
import messages._
import org.jsoup.Jsoup

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Fetcher extends Actor {

  override def receive: Receive = {
    case Fetch(url) =>
      Future {Jsoup.connect(url).get()}
        .map(document => Fetched(url, document))
        .pipeTo(sender())
  }
}
