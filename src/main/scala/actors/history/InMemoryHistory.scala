package actors.history

import messages.{Clean, NewURL, Stats, StatsResponse, ValidateAsNewURL}

case class InMemoryHistory() extends History {

  var history: List[String] = List()
  val start: Long = System.currentTimeMillis()

  override def receive: Receive = {
    case ValidateAsNewURL(url) =>

      if(!history.exists(_.equals(url))) {
        history = url :: history
        sender ! NewURL(url)
      }

    case Clean() =>
      history = List()
      sender ! "done"

    case Stats() =>
      val time = System.currentTimeMillis() - start
      sender ! StatsResponse(history.size, history.size / (time / 1000) toInt, time / history.size toInt, (System.currentTimeMillis() - start)/ 1000 toInt)
  }
}
