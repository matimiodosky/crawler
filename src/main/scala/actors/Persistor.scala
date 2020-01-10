package actors

import java.util.UUID

import akka.actor.Actor
import messages.{ValidateVisited, ValidatedUnvisited}
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._

import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.pipe

import scala.util.Success


class Persistor extends Actor {

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

    case ValidateVisited(url) => collection
      .find(equal("url", url))
      .collect()
      .toFuture()
      .filter(seq => seq.isEmpty)
      .map(seq => collection
        .insertOne(Document("_id" -> UUID.randomUUID().toString, "url" -> url))
        .toFuture())
      .map(_ => ValidatedUnvisited(url))
      .pipeTo(sender)

  }

}
