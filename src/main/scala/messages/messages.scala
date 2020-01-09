package messages

import org.jsoup.nodes.Document

case class Start(url: String)

case class Fetch(url: String)

case class Fetched(url: String, html: Document)

case class Parse(url: String, html: Document)

case class Parsed(urls: List[String])

case class Print(string: String)

case class StatsRequest(lastCount: Int)

case class StatsResponse(currentCount: Int, newsCount: Int)

case class DumpResponse(urls: List[String])

case class DumpRequest()

case class Tick()
