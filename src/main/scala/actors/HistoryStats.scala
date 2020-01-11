package actors

import akka.actor.Actor
import messages.{Stats, StatsResponse}
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.pipe


class HistoryStats extends Actor with MongoAccessor {
  val start: Long = System.currentTimeMillis()

  override def receive: Receive = {
    case Stats() =>
      val time = System.currentTimeMillis() - start
      collection
        .countDocuments()
        .toFuture()
        .map(count => StatsResponse(count toInt, count / (time / 1000) toInt, time / count toInt, (System.currentTimeMillis() - start)/ 1000 toInt))
        .pipeTo(sender)

  }
}
