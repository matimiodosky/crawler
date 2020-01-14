package actors

import java.net.URL

import akka.actor.Actor
import org.jsoup.nodes.Document

import scala.util.Try
import messages._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.pipe

class Parser extends Actor {

  import scala.jdk.CollectionConverters._

  def getUrls(url: String, html: Document): List[String] = {
    val current = new URL(url)
    val doc = org.jsoup.Jsoup.parse(html.toString)
    val links =
      doc
        .select("a[href]")
        .asScala
        .map(_.attr("href"))

    links
      .flatMap { l =>
        Try(new URL(current, l)).toOption
      }
      .map(_.toString.takeWhile(_ != '#'))
      .distinct
      .toList
  }

  override def receive: Receive = {

    case Parse(url, html) =>
      Future {getUrls(url, html)}
          .map(urls => Parsed(url, urls))
          .pipeTo(sender)
  }

}
