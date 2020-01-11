package messages

import org.jsoup.nodes.Document

case class Start(url: String)

case class Fetch(url: String)

case class Fetched(url: String, html: Document)

case class Parse(url: String, html: Document)

case class Parsed(urls: List[String])

case class ValidateAsNewURL(url: String)

case class NewURL(url: String)

case class Stats()

case class StatsResponse(count: Int, perSecond: Int, perURL: Int)

case class Clean()


