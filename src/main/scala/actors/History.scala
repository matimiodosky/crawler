package actors

import java.util.UUID

import akka.actor.Actor
import akka.pattern.pipe
import messages.{ValidateAsNewURL, NewURL}
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

class History extends Actor {

  val mongoClient: MongoClient = MongoClient()

  val settings: MongoClientSettings = MongoClientSettings.builder()
    .applyToClusterSettings(b => b.hosts(List(new ServerAddress("localhost")).asJava))
    .build()

  val database: MongoDatabase = mongoClient.getDatabase("crawler")

  val collection: MongoCollection[Document] = database.getCollection("visited")

  val noOpObserver: Observer[Completed] = new Observer[Completed] {

    override def onNext(result: Completed): Unit = {
      //      println(result)
    }

    override def onError(e: Throwable): Unit = {
      //      println(e)
    }

    override def onComplete(): Unit = {
      //      println("completed")
    }
  }

  override def receive: Receive = {

    case ValidateAsNewURL(url) => collection
      .find(equal("url", url))
      .collect()
      .toFuture()
      .filter(seq => seq.isEmpty)
      .map(seq => collection
        .insertOne(Document("_id" -> UUID.randomUUID().toString, "url" -> url))
        .toFuture())
      .map(_ => NewURL(url))
      .pipeTo(sender)

  }

}
