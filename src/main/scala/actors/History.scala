package actors

import java.util.logging.{Level, Logger}

import akka.actor.{Actor, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router, SmallestMailboxRoutingLogic}
import messages._
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import akka.pattern.pipe

case class History(workersCount: Int) extends Actor with MongoAccessor {

  val start: Long = System.currentTimeMillis()

  Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING)

  var router: Router = {
    val workers = Vector.fill(8) {
      val r = context.actorOf(Props[HistoryWorker])
      context.watch(r)
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), workers)
  }

  override def receive: Receive = {

    case Clean() => collection.drop().toFuture().map(_ => "cleaned").pipeTo(sender)

    case work =>
      router.route(work, sender)

  }

}

class HistoryWorker extends Actor with MongoAccessor {

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


