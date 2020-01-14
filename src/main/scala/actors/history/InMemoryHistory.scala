package actors.history

import messages._

import scala.collection.mutable.{Set => MutableSet}
import  scala.language.postfixOps

case class InMemoryHistory() extends History {

  var history: MutableSet[String] = MutableSet()
  val start: Long = System.currentTimeMillis()
  var lastTime: Long = start
  var lastCount: Int = 0

  override def receive: Receive = {
    case ValidateAsNewURL(url) =>
      history.find(_.equals(url)) match {
        case None =>
          sender ! NewURL(url)
          history.add(url)
        case _ =>
      }

    case Clean() =>
      history = MutableSet()
      sender ! "done"

    case Stats() =>
      try {
        val time = System.currentTimeMillis() - start
        val response = StatsResponse(history.size, history.size / (time / 1000) toInt, time / history.size toInt, (System.currentTimeMillis() - start) / 1000 toInt)
        println("Time: " + time / 1000 + " Count: " + response.count + " -- Per Second: " + response.perSecond + "  -- Per URL (millis): " + response.perURL + "-- Per Second Latetly: " + (response.count - lastCount) / ((time - lastTime) / 1000))
        lastTime = time
        lastCount = response.count
      }catch {
        case _: Throwable =>
      }

  }

}
