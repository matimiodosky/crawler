package actors.history

import java.util.logging.{Level, Logger}

import actors.MongoAccessor
import akka.actor.{Actor, Props}
import akka.pattern.pipe
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import messages._
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import util.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

case class MongoHistory() extends History with MongoAccessor {

  val start: Long = System.currentTimeMillis()
  var lastTime: Long = start
  var lastCount: Int = 0

  Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING)

  var router: Router = {
    val workers = Vector.fill(Configuration.getConfig("mongoHistoryWorkers") toInt) {
      val r = context.actorOf(Props[MongoHistoryWorker])
      context.watch(r)
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), workers)
  }

  override def receive: Receive = {

    case Clean() => collection.drop().toFuture().map(_ => "cleaned").pipeTo(sender)

    case Stats() =>
      val time = System.currentTimeMillis() - start
      collection
        .countDocuments()
        .toFuture()
        .map(count => StatsResponse(count toInt, count / (time / 1000) toInt, time / count toInt, (System.currentTimeMillis() - start) / 1000 toInt))
        .foreach(response => {

          println("Time: " + time / 1000 + " Count: " + response.count + " -- Per Second: " + response.perSecond + "  -- Per URL (millis): " + response.perURL + "-- Per Second Latetly: " + (response.count - lastCount) / ((time - lastTime) / 1000) )
          lastTime = time
          lastCount = response.count
        }
        )


    case work =>
      router.route(work, sender)

  }

}

class MongoHistoryWorker extends Actor with MongoAccessor {

  val start: Long = System.currentTimeMillis()

  override def receive: Receive = {

    case ValidateAsNewURL(url) =>
      collection
        .countDocuments(equal("_id", url))
        .toFuture()
        .filter(count => count < 1)
        .map(_ => collection
          .insertOne(Document("_id" -> url))
          .toFuture())
        .map(_ => NewURL(url))
        .pipeTo(sender)

  }
}


