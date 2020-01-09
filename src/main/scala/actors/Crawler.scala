package actors

import java.net.URI

import akka.actor.{Actor, ActorRef, Props}
import messages._


class Crawler extends Actor {

  var visited: List[String] = List()
  val fetcher: ActorRef = context.actorOf(Props[Fetcher])
  val parser: ActorRef = context.actorOf(Props[Parser])
  var client: ActorRef = ActorRef.noSender

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
      visited = url :: visited
      fetcher ! Fetch(url)

    case Fetched(url, html) =>
      parser ! Parse(url, html)

    case Parsed(urls) =>
      val newUrls = urls.filter(url => !visited.contains(url))

      newUrls.foreach(url => {
        fetcher ! Fetch(url)
        client ! Print(showUrl(url) + "    " + url)
      })

      visited = visited ::: newUrls

    case StatsRequest(lastCount) =>
      sender ! StatsResponse(visited.size, visited.size - lastCount)

    case DumpRequest() =>
      sender ! DumpResponse(visited)

  }

}
