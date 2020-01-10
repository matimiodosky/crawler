package actors

import java.net.URI

import akka.actor.{Actor, ActorRef, Props}
import messages._


class Crawler extends Actor {

  val fetcher: ActorRef = context.actorOf(Props[Fetcher])
  val parser: ActorRef = context.actorOf(Props[Parser])
  var client: ActorRef = ActorRef.noSender
  var persistor: ActorRef = context.actorOf(Props[Persistor])

  def showUrl(url: String): String = {
    try {
      val uri = new URI(url)
      val domain = uri.getHost
      if (domain.startsWith("www.")) domain.substring(4) else domain
    } catch {
      case _: Exception => ""
    }
  }

  override def receive: Receive = {

    case Start(url) =>
      client = sender
      persistor ! ValidateVisited(url)

    case Fetched(url, html) =>
      parser ! Parse(url, html)

    case Parsed(urls) =>

      urls.foreach(url => {
        persistor ! ValidateVisited(url)
      })

    case ValidatedUnvisited(url) =>
      fetcher ! Fetch(url)
      client ! Print(showUrl(url) + "    " + url)

  }

}
